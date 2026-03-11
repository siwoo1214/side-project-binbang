import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAccommodationList } from '../api/accommodationApi';
import { getCategories, getTopRegions } from '../api/categoryApi';
import type { AccommodationListItem, AccommodationListParams } from '../types/accommodation';
import type { Category, RegionResponse } from '../types/category';
import Header from '../components/common/Header';
import styles from './HomePage.module.css';

// 가격 포맷 (100000 → "100,000원")
const formatPrice = (price: number) =>
  `${price.toLocaleString('ko-KR')}원`;

// 이미지 없을 때 보여줄 placeholder
const PLACEHOLDER_IMG = 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=400&q=80';

export default function HomePage() {
  const [accommodations, setAccommodations] = useState<AccommodationListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [searchInput, setSearchInput] = useState('');

  // 필터 상태
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [selectedRegion, setSelectedRegion] = useState<number | null>(null);
  const [filterPet, setFilterPet] = useState(false);
  const [filterWifi, setFilterWifi] = useState(false);
  const [filterParking, setFilterParking] = useState(false);
  const [filterBbq, setFilterBbq] = useState(false);

  // 카테고리 / 지역 데이터
  const [categories, setCategories] = useState<Category[]>([]);
  const [regions, setRegions] = useState<RegionResponse[]>([]);

  // 로그인한 사용자 이메일 (localStorage에서)
  const userEmail = localStorage.getItem('accessToken')
    ? (() => {
        try {
          // JWT payload 디코딩 (base64)
          const payload = localStorage.getItem('accessToken')!.split('.')[1];
          const decoded = JSON.parse(atob(payload));
          return decoded.sub as string; // sub = email
        } catch {
          return null;
        }
      })()
    : null;

  // 카테고리 + 상위 지역 초기 로딩
  useEffect(() => {
    Promise.all([getCategories(), getTopRegions()])
      .then(([cats, regs]) => {
        setCategories(cats);
        setRegions(regs);
      })
      .catch(() => {/* 필터 로딩 실패는 조용히 무시 */});
  }, []);

  const fetchAccommodations = async (params: AccommodationListParams = {}) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getAccommodationList({ size: 12, ...params });
      setAccommodations(data.content);
      setTotalPages(data.totalPages);
    } catch {
      setError('숙소 목록을 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  // 초기 로딩 + 필터/검색어/페이지 변경 시
  useEffect(() => {
    fetchAccommodations({
      page: currentPage,
      keyword: keyword || undefined,
      categoryId: selectedCategory ?? undefined,
      regionId: selectedRegion ?? undefined,
      petAllowed: filterPet || undefined,
      hasWifi: filterWifi || undefined,
      parkingAvailable: filterParking || undefined,
      hasBbq: filterBbq || undefined,
    });
  }, [currentPage, keyword, selectedCategory, selectedRegion, filterPet, filterWifi, filterParking, filterBbq]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    setKeyword(searchInput.trim());
  };

  const handleFilterChange = () => {
    setCurrentPage(0);
  };

  const handleResetFilters = () => {
    setSelectedCategory(null);
    setSelectedRegion(null);
    setFilterPet(false);
    setFilterWifi(false);
    setFilterParking(false);
    setFilterBbq(false);
    setKeyword('');
    setSearchInput('');
    setCurrentPage(0);
  };

  const hasActiveFilter = !!keyword || selectedCategory !== null || selectedRegion !== null
    || filterPet || filterWifi || filterParking || filterBbq;

  return (
    <div className={styles.page}>
      {/* ── 공통 헤더 ── */}
      <Header />

      {/* ── 히어로 ── */}
      <section className={styles.hero}>
        <h1 className={styles.heroTitle}>
          {userEmail
            ? <>{userEmail.split('@')[0]}님,<br /><em>어디로 떠나시게요?</em></>
            : <>어디로<br /><em>떠나고 싶으세요?</em></>}
        </h1>
        <p className={styles.heroSub}>
          {userEmail
            ? `${userEmail} · 전국의 빈 방을 한 곳에서`
            : '전국의 빈 방을 한 곳에서'}
        </p>

        {/* ── 검색 + 필터 ── */}
        <div className={styles.searchSection}>
          {/* 검색바 */}
          <form className={styles.searchBar} onSubmit={handleSearch}>
            <span className={styles.searchIcon}>🔍</span>
            <input
              type="text"
              className={styles.searchInput}
              value={searchInput}
              onChange={e => setSearchInput(e.target.value)}
              placeholder="숙소명, 지역명으로 검색..."
            />
            <button type="submit" className={styles.searchButton}>검색</button>
          </form>

          {/* 필터 행 */}
          <div className={styles.filterRow}>
            {/* 카테고리 */}
            <select
              className={styles.filterSelect}
              value={selectedCategory ?? ''}
              onChange={e => {
                setSelectedCategory(e.target.value ? Number(e.target.value) : null);
                handleFilterChange();
              }}
            >
              <option value="">카테고리 전체</option>
              {categories.map(c => (
                <option key={c.categoryId} value={c.categoryId}>{c.name}</option>
              ))}
            </select>

            {/* 지역 */}
            <select
              className={styles.filterSelect}
              value={selectedRegion ?? ''}
              onChange={e => {
                setSelectedRegion(e.target.value ? Number(e.target.value) : null);
                handleFilterChange();
              }}
            >
              <option value="">지역 전체</option>
              {regions.map(r => (
                <option key={r.regionId} value={r.regionId}>{r.name}</option>
              ))}
            </select>

            {/* 시설 토글 */}
            {([
              { key: 'pet',     label: '🐾 반려동물', state: filterPet,     setter: setFilterPet },
              { key: 'wifi',    label: '📶 Wi-Fi',    state: filterWifi,    setter: setFilterWifi },
              { key: 'parking', label: '🚗 주차',     state: filterParking, setter: setFilterParking },
              { key: 'bbq',     label: '🔥 BBQ',      state: filterBbq,     setter: setFilterBbq },
            ] as const).map(({ key, label, state, setter }) => (
              <button
                key={key}
                type="button"
                className={`${styles.filterChip} ${state ? styles.filterChipActive : ''}`}
                onClick={() => { setter(v => !v); handleFilterChange(); }}
              >
                {label}
              </button>
            ))}

            {/* 초기화 버튼 (활성 필터 있을 때만) */}
            {hasActiveFilter && (
              <button className={styles.resetBtn} onClick={handleResetFilters}>
                초기화 ✕
              </button>
            )}
          </div>
        </div>
      </section>

      {/* ── 메인 컨텐츠 ── */}
      <main className={styles.main}>
        {/* 검색 결과 헤더 */}
        {hasActiveFilter && (
          <div className={styles.searchResultBar}>
            <span>
              {keyword && <><strong>"{keyword}"</strong> 검색{' '}</>}
              {(selectedCategory !== null || selectedRegion !== null || filterPet || filterWifi || filterParking || filterBbq)
                && '필터 적용 중'}
            </span>
          </div>
        )}

        {/* 로딩 */}
        {isLoading && (
          <div className={styles.stateBox}>
            <div className={styles.loadingGrid}>
              {Array.from({ length: 8 }).map((_, i) => (
                <div key={i} className={styles.skeleton} />
              ))}
            </div>
          </div>
        )}

        {/* 에러 */}
        {!isLoading && error && (
          <div className={styles.stateBox}>
            <p className={styles.errorText}>{error}</p>
            <button className={styles.retryButton} onClick={() => fetchAccommodations()}>
              다시 시도
            </button>
          </div>
        )}

        {/* 빈 결과 */}
        {!isLoading && !error && accommodations.length === 0 && (
          <div className={styles.stateBox}>
            <p className={styles.emptyIcon}>🏡</p>
            <p className={styles.emptyText}>검색 결과가 없어요</p>
            <p className={styles.emptySubText}>다른 키워드로 검색해보세요</p>
          </div>
        )}

        {/* 숙소 카드 그리드 */}
        {!isLoading && !error && accommodations.length > 0 && (
          <>
            <div className={styles.grid}>
              {accommodations.map(acc => (
                <Link
                  key={acc.accommodationId}
                  to={`/accommodations/${acc.accommodationId}`}
                  className={styles.card}
                >
                  {/* 이미지 */}
                  <div className={styles.cardImageWrap}>
                    <img
                      src={acc.thumbnailUrl ?? PLACEHOLDER_IMG}
                      alt={acc.name}
                      className={styles.cardImage}
                      loading="lazy"
                      onError={e => { (e.target as HTMLImageElement).src = PLACEHOLDER_IMG; }}
                    />
                    <span className={styles.categoryBadge}>{acc.categoryName}</span>
                  </div>

                  {/* 정보 */}
                  <div className={styles.cardBody}>
                    <p className={styles.cardRegion}>📍 {acc.regionName}</p>
                    <h3 className={styles.cardName}>{acc.name}</h3>
                    <p className={styles.cardPrice}>
                      <strong>{formatPrice(acc.price)}</strong>
                      <span> / 박</span>
                    </p>
                  </div>
                </Link>
              ))}
            </div>

            {/* 페이지네이션 */}
            {totalPages > 1 && (
              <div className={styles.pagination}>
                <button
                  className={styles.pageButton}
                  onClick={() => setCurrentPage(p => p - 1)}
                  disabled={currentPage === 0}
                >
                  ← 이전
                </button>

                {Array.from({ length: totalPages }).map((_, i) => (
                  <button
                    key={i}
                    className={`${styles.pageButton} ${i === currentPage ? styles.pageButtonActive : ''}`}
                    onClick={() => setCurrentPage(i)}
                  >
                    {i + 1}
                  </button>
                ))}

                <button
                  className={styles.pageButton}
                  onClick={() => setCurrentPage(p => p + 1)}
                  disabled={currentPage === totalPages - 1}
                >
                  다음 →
                </button>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
