import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Header from '../components/common/Header';
import { getAccommodationDetail } from '../api/accommodationApi';
import { createReservation } from '../api/reservationApi';
import { toggleWishlist } from '../api/wishlistApi';
import type { AccommodationDetail } from '../types/accommodation';
import styles from './AccommodationDetailPage.module.css';

// 시설 아이콘 + 레이블 매핑
const FACILITY_ITEMS = [
  { key: 'hasWifi',         label: 'Wi-Fi',      icon: '📶' },
  { key: 'parkingAvailable',label: '주차 가능',   icon: '🚗' },
  { key: 'petAllowed',      label: '반려동물 허용', icon: '🐾' },
  { key: 'hasBbq',          label: 'BBQ 가능',    icon: '🔥' },
] as const;

export default function AccommodationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  // ── 숙소 데이터 상태 ──
  const [accommodation, setAccommodation] = useState<AccommodationDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // ── 이미지 슬라이더 상태 ──
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  // ── 위시리스트 상태 ──
  const [isWishlisted, setIsWishlisted] = useState(false);
  const [wishlistLoading, setWishlistLoading] = useState(false);

  // ── 예약 폼 상태 ──
  const [checkIn, setCheckIn]     = useState('');
  const [checkOut, setCheckOut]   = useState('');
  const [guestCount, setGuestCount] = useState(1);
  const [reserving, setReserving] = useState(false);
  const [reserveError, setReserveError] = useState<string | null>(null);
  const [reserveSuccess, setReserveSuccess] = useState(false);

  // ── 숙소 데이터 로드 ──
  useEffect(() => {
    if (!id) return;
    const fetchDetail = async () => {
      try {
        setLoading(true);
        const data = await getAccommodationDetail(Number(id));
        setAccommodation(data);
      } catch {
        setError('숙소 정보를 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  // ── 예약 가능 여부 계산 ──
  const calcNights = (): number => {
    if (!checkIn || !checkOut) return 0;
    const diff = (new Date(checkOut).getTime() - new Date(checkIn).getTime()) / (1000 * 60 * 60 * 24);
    return diff > 0 ? diff : 0;
  };

  const nights = calcNights();
  const totalPrice = accommodation ? accommodation.price * nights : 0;

  // ── 예약 제출 ──
  const handleReserve = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      navigate('/login');
      return;
    }
    if (!checkIn || !checkOut || nights <= 0) {
      setReserveError('체크인/체크아웃 날짜를 올바르게 선택해주세요.');
      return;
    }
    if (!accommodation) return;

    try {
      setReserving(true);
      setReserveError(null);
      await createReservation({
        accommodationId: accommodation.accommodationId,
        checkInDate: checkIn,
        checkOutDate: checkOut,
        guestCount,
      });
      setReserveSuccess(true);
    } catch (err: any) {
      const msg = err.response?.data?.message || '예약에 실패했습니다. 다시 시도해주세요.';
      setReserveError(msg);
    } finally {
      setReserving(false);
    }
  };

  // ── 위시리스트 토글 ──
  const handleWishlistToggle = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      navigate('/login');
      return;
    }
    if (!accommodation) return;

    try {
      setWishlistLoading(true);
      const result = await toggleWishlist(accommodation.accommodationId);
      setIsWishlisted(result.isWishlisted);
    } catch {
      alert('위시리스트 처리 중 오류가 발생했습니다.');
    } finally {
      setWishlistLoading(false);
    }
  };

  // ── 시간 포맷 (HH:mm:ss → HH:mm) ──
  const formatTime = (time: string) => time?.substring(0, 5) ?? '';

  // ── 가격 포맷 ──
  const formatPrice = (price: number) => price.toLocaleString('ko-KR');

  // ── 로딩 ──
  if (loading) return (
    <div className={styles.page}>
      <Header />
      <div className={styles.skeleton}>
        <div className={styles.skeletonImage} />
        <div className={styles.skeletonContent}>
          <div className={styles.skeletonLine} style={{ width: '60%', height: '2rem' }} />
          <div className={styles.skeletonLine} style={{ width: '40%' }} />
          <div className={styles.skeletonLine} style={{ width: '80%' }} />
        </div>
      </div>
    </div>
  );

  // ── 에러 ──
  if (error || !accommodation) return (
    <div className={styles.page}>
      <Header />
      <div className={styles.errorWrap}>
        <p className={styles.errorMsg}>{error ?? '숙소를 찾을 수 없습니다.'}</p>
        <button className={styles.backBtn} onClick={() => navigate('/')}>홈으로 돌아가기</button>
      </div>
    </div>
  );

  const images = accommodation.imageUrls.length > 0
    ? accommodation.imageUrls
    : [null]; // 이미지 없을 때 플레이스홀더

  return (
    <div className={styles.page}>
      <Header />

      {/* ── 이미지 섹션 ── */}
      <section className={styles.imageSection}>
        <div className={styles.mainImage}>
          {images[currentImageIndex] ? (
            <img src={images[currentImageIndex]!} alt={accommodation.name} />
          ) : (
            <div className={styles.imagePlaceholder}>
              <span>🏠</span>
              <p>등록된 이미지가 없습니다</p>
            </div>
          )}
          {images.length > 1 && (
            <>
              <button
                className={`${styles.imgNav} ${styles.imgNavPrev}`}
                onClick={() => setCurrentImageIndex(i => (i - 1 + images.length) % images.length)}
              >‹</button>
              <button
                className={`${styles.imgNav} ${styles.imgNavNext}`}
                onClick={() => setCurrentImageIndex(i => (i + 1) % images.length)}
              >›</button>
              <div className={styles.imgDots}>
                {images.map((_, idx) => (
                  <button
                    key={idx}
                    className={`${styles.dot} ${idx === currentImageIndex ? styles.dotActive : ''}`}
                    onClick={() => setCurrentImageIndex(idx)}
                  />
                ))}
              </div>
            </>
          )}
        </div>

        {/* 썸네일 목록 (이미지 2장 이상일 때) */}
        {images.length > 1 && (
          <div className={styles.thumbnailRow}>
            {images.map((url, idx) => (
              <button
                key={idx}
                className={`${styles.thumbnail} ${idx === currentImageIndex ? styles.thumbnailActive : ''}`}
                onClick={() => setCurrentImageIndex(idx)}
              >
                {url ? <img src={url} alt={`이미지 ${idx + 1}`} /> : <span>🏠</span>}
              </button>
            ))}
          </div>
        )}
      </section>

      {/* ── 메인 콘텐츠 ── */}
      <div className={styles.contentWrap}>

        {/* ── 왼쪽: 숙소 정보 ── */}
        <main className={styles.main}>

          <div className={styles.infoHeader}>
            <div className={styles.badges}>
              <span className={styles.badge}>{accommodation.categoryName}</span>
              <span className={styles.badge}>{accommodation.regionName}</span>
            </div>
            <div className={styles.titleRow}>
              <h1 className={styles.title}>{accommodation.name}</h1>
              <button
                className={`${styles.heartBtn} ${isWishlisted ? styles.heartBtnActive : ''}`}
                onClick={handleWishlistToggle}
                disabled={wishlistLoading}
                title={isWishlisted ? '위시리스트에서 제거' : '위시리스트에 추가'}
              >
                {isWishlisted ? '❤️' : '🩶'}
              </button>
            </div>
            <p className={styles.address}>📍 {accommodation.address}</p>
            <p className={styles.host}>호스트: <strong>{accommodation.hostName}</strong></p>
          </div>

          <hr className={styles.divider} />

          {/* 체크인/아웃 시간 */}
          <div className={styles.checkTimes}>
            <div className={styles.checkTimeItem}>
              <span className={styles.checkTimeLabel}>체크인</span>
              <span className={styles.checkTimeValue}>{formatTime(accommodation.checkInTime)}</span>
            </div>
            <div className={styles.checkTimeDivider} />
            <div className={styles.checkTimeItem}>
              <span className={styles.checkTimeLabel}>체크아웃</span>
              <span className={styles.checkTimeValue}>{formatTime(accommodation.checkOutTime)}</span>
            </div>
          </div>

          <hr className={styles.divider} />

          {/* 시설 정보 */}
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>시설 안내</h2>
            <div className={styles.facilityGrid}>
              {accommodation.bedrooms != null && (
                <div className={styles.facilityItem}>
                  <span className={styles.facilityIcon}>🛏</span>
                  <span>침실 {accommodation.bedrooms}개</span>
                </div>
              )}
              {accommodation.beds != null && (
                <div className={styles.facilityItem}>
                  <span className={styles.facilityIcon}>🛌</span>
                  <span>침대 {accommodation.beds}개</span>
                </div>
              )}
              {accommodation.bathrooms != null && (
                <div className={styles.facilityItem}>
                  <span className={styles.facilityIcon}>🚿</span>
                  <span>욕실 {accommodation.bathrooms}개</span>
                </div>
              )}
              {FACILITY_ITEMS.map(item => {
                const val = accommodation[item.key as keyof AccommodationDetail];
                if (!val) return null;
                return (
                  <div key={item.key} className={styles.facilityItem}>
                    <span className={styles.facilityIcon}>{item.icon}</span>
                    <span>{item.label}</span>
                  </div>
                );
              })}
            </div>
          </section>

          <hr className={styles.divider} />

          {/* 숙소 설명 */}
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>숙소 소개</h2>
            <p className={styles.description}>{accommodation.description}</p>
          </section>
        </main>

        {/* ── 오른쪽: 예약 카드 ── */}
        <aside className={styles.reserveCard}>
          <div className={styles.priceRow}>
            <span className={styles.price}>₩{formatPrice(accommodation.price)}</span>
            <span className={styles.priceUnit}> / 박</span>
          </div>

          {reserveSuccess ? (
            <div className={styles.successBox}>
              <p className={styles.successIcon}>🎉</p>
              <p className={styles.successTitle}>예약이 완료되었습니다!</p>
              <p className={styles.successSub}>이메일로 예약 확인서가 발송되었습니다.</p>
              <button className={styles.successBtn} onClick={() => navigate('/')}>
                홈으로 돌아가기
              </button>
            </div>
          ) : (
            <>
              <div className={styles.dateInputGroup}>
                <div className={styles.dateField}>
                  <label className={styles.dateLabel}>체크인</label>
                  <input
                    type="date"
                    className={styles.dateInput}
                    value={checkIn}
                    min={new Date().toISOString().split('T')[0]}
                    onChange={e => {
                      setCheckIn(e.target.value);
                      if (checkOut && e.target.value >= checkOut) setCheckOut('');
                    }}
                  />
                </div>
                <div className={styles.dateField}>
                  <label className={styles.dateLabel}>체크아웃</label>
                  <input
                    type="date"
                    className={styles.dateInput}
                    value={checkOut}
                    min={checkIn || new Date().toISOString().split('T')[0]}
                    onChange={e => setCheckOut(e.target.value)}
                  />
                </div>
              </div>

              <div className={styles.guestField}>
                <label className={styles.dateLabel}>인원</label>
                <div className={styles.counter}>
                  <button
                    className={styles.counterBtn}
                    onClick={() => setGuestCount(n => Math.max(1, n - 1))}
                  >−</button>
                  <span className={styles.counterValue}>{guestCount}명</span>
                  <button
                    className={styles.counterBtn}
                    onClick={() => setGuestCount(n => Math.min(20, n + 1))}
                  >+</button>
                </div>
              </div>

              {nights > 0 && (
                <div className={styles.priceCalc}>
                  <div className={styles.priceCalcRow}>
                    <span>₩{formatPrice(accommodation.price)} × {nights}박</span>
                    <span>₩{formatPrice(totalPrice)}</span>
                  </div>
                  <hr className={styles.calcDivider} />
                  <div className={`${styles.priceCalcRow} ${styles.priceCalcTotal}`}>
                    <span>합계</span>
                    <span>₩{formatPrice(totalPrice)}</span>
                  </div>
                </div>
              )}

              {reserveError && (
                <p className={styles.reserveError}>{reserveError}</p>
              )}

              <button
                className={styles.reserveBtn}
                onClick={handleReserve}
                disabled={reserving}
              >
                {reserving ? '예약 중...' : '예약하기'}
              </button>

              {!localStorage.getItem('accessToken') && (
                <p className={styles.loginHint}>
                  예약하려면 <button className={styles.loginLink} onClick={() => navigate('/login')}>로그인</button>이 필요합니다.
                </p>
              )}
            </>
          )}
        </aside>
      </div>
    </div>
  );
}
