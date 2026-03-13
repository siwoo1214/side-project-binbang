import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/common/Header';
import { getMyAccommodations } from '../api/accommodationApi';
import type { AccommodationListItem } from '../types/accommodation';
import styles from './MyAccommodationsPage.module.css';

// 가격 포맷
const formatPrice = (price: number) => price.toLocaleString('ko-KR');

export default function MyAccommodationsPage() {
    const navigate = useNavigate();

    const [accommodations, setAccommodations] = useState<AccommodationListItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // ── 내 숙소 목록 로드 ──
    useEffect(() => {
        const fetch = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await getMyAccommodations();
                setAccommodations(data);
            } catch {
                setError('숙소 목록을 불러오지 못했습니다.');
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, []);

    return (
        <div className={styles.page}>
            <Header />
            <main className={styles.main}>

                {/* ── 페이지 헤더 ── */}
                <div className={styles.pageHeader}>
                    <h1 className={styles.title}>내 숙소</h1>
                    <button
                        className={styles.registerBtn}
                        onClick={() => navigate('/accommodations/register')}
                    >
                        + 숙소 등록
                    </button>
                </div>

                {/* ── 로딩 스켈레톤 ── */}
                {loading && (
                    <div className={styles.grid}>
                        {Array.from({ length: 4 }).map((_, i) => (
                            <div key={i} className={styles.skeletonCard}>
                                <div className={styles.skeletonImage} />
                                <div className={styles.skeletonContent}>
                                    <div className={styles.skeletonLine} style={{ width: '60%' }} />
                                    <div className={styles.skeletonLine} style={{ width: '40%' }} />
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {/* ── 에러 ── */}
                {!loading && error && (
                    <div className={styles.empty}>
                        <p className={styles.emptyIcon}>⚠️</p>
                        <p className={styles.emptyText}>{error}</p>
                    </div>
                )}

                {/* ── 빈 상태 ── */}
                {!loading && !error && accommodations.length === 0 && (
                    <div className={styles.empty}>
                        <p className={styles.emptyIcon}>🏠</p>
                        <p className={styles.emptyText}>등록된 숙소가 없어요</p>
                        <p className={styles.emptySub}>첫 번째 숙소를 등록해 게스트를 맞이해보세요</p>
                        <button
                            className={styles.exploreBtn}
                            onClick={() => navigate('/accommodations/register')}
                        >
                            숙소 등록하기
                        </button>
                    </div>
                )}

                {/* ── 숙소 카드 그리드 ── */}
                {!loading && !error && accommodations.length > 0 && (
                    <div className={styles.grid}>
                        {accommodations.map(accommodation => (
                            <div
                                key={accommodation.accommodationId}
                                className={styles.card}
                                onClick={() => navigate(`/accommodations/${accommodation.accommodationId}`)}
                            >
                                {/* 썸네일 이미지 */}
                                <div className={styles.imageWrap}>
                                    {accommodation.thumbnailUrl ? (
                                        <img
                                            src={accommodation.thumbnailUrl}
                                            alt={accommodation.name}
                                            className={styles.image}
                                        />
                                    ) : (
                                        <div className={styles.imagePlaceholder}>
                                            <span>🏠</span>
                                        </div>
                                    )}
                                </div>

                                {/* 카드 정보 */}
                                <div className={styles.cardContent}>
                                    <p className={styles.cardRegion}>
                                        {accommodation.regionName} · {accommodation.categoryName}
                                    </p>
                                    <p className={styles.cardName}>{accommodation.name}</p>
                                    <p className={styles.cardPrice}>
                                        ₩{formatPrice(accommodation.price)}
                                        <span className={styles.perNight}> / 박</span>
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

            </main>
        </div>
    );
}
