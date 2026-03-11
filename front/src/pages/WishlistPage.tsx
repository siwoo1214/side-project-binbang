import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/common/Header';
import { getMyWishlists, toggleWishlist } from '../api/wishlistApi';
import type { WishlistItem } from '../types/wishlist';
import styles from './WishlistPage.module.css';

// 시간 포맷 (HH:mm:ss → HH:mm)
const formatTime = (time: string) => time?.substring(0, 5) ?? '';

// 가격 포맷
const formatPrice = (price: number) => price.toLocaleString('ko-KR');

export default function WishlistPage() {
    const navigate = useNavigate();

    const [wishlists, setWishlists] = useState<WishlistItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [removingId, setRemovingId] = useState<number | null>(null); // 삭제 중인 항목 ID

    // ── 위시리스트 목록 로드 ──
    useEffect(() => {
        const fetch = async () => {
            try {
                setLoading(true);
                const data = await getMyWishlists();
                setWishlists(data);
            } catch {
                setError('위시리스트를 불러오지 못했습니다.');
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, []);

    // ── 위시리스트에서 제거 ──
    const handleRemove = async (item: WishlistItem) => {
        try {
            setRemovingId(item.listId);
            await toggleWishlist(item.accommodationId);
            // 성공 시 목록에서 즉시 제거 (서버 재조회 없이)
            setWishlists(prev => prev.filter(w => w.listId !== item.listId));
        } catch {
            alert('위시리스트 제거 중 오류가 발생했습니다.');
        } finally {
            setRemovingId(null);
        }
    };

    // ── 로딩 ──
    if (loading) return (
        <div className={styles.page}>
            <Header />
            <main className={styles.main}>
                <h1 className={styles.title}>위시리스트</h1>
                <div className={styles.skeletonList}>
                    {Array.from({ length: 4 }).map((_, i) => (
                        <div key={i} className={styles.skeletonItem}>
                            <div className={styles.skeletonImage} />
                            <div className={styles.skeletonContent}>
                                <div className={styles.skeletonLine} style={{ width: '60%' }} />
                                <div className={styles.skeletonLine} style={{ width: '40%' }} />
                                <div className={styles.skeletonLine} style={{ width: '30%' }} />
                            </div>
                        </div>
                    ))}
                </div>
            </main>
        </div>
    );

    // ── 에러 ──
    if (error) return (
        <div className={styles.page}>
            <Header />
            <main className={styles.main}>
                <h1 className={styles.title}>위시리스트</h1>
                <div className={styles.empty}>
                    <p className={styles.emptyIcon}>⚠️</p>
                    <p className={styles.emptyText}>{error}</p>
                </div>
            </main>
        </div>
    );

    return (
        <div className={styles.page}>
            <Header />
            <main className={styles.main}>
                <div className={styles.titleRow}>
                    <h1 className={styles.title}>위시리스트</h1>
                    <span className={styles.count}>{wishlists.length}개</span>
                </div>

                {/* ── 빈 상태 ── */}
                {wishlists.length === 0 ? (
                    <div className={styles.empty}>
                        <p className={styles.emptyIcon}>🤍</p>
                        <p className={styles.emptyText}>아직 저장된 숙소가 없어요</p>
                        <p className={styles.emptySub}>마음에 드는 숙소의 ❤️ 버튼을 눌러 저장해보세요</p>
                        <button className={styles.exploreBtn} onClick={() => navigate('/')}>
                            숙소 둘러보기
                        </button>
                    </div>
                ) : (
                    <ul className={styles.list}>
                        {wishlists.map(item => (
                            <li key={item.listId} className={styles.card}>
                                {/* 숙소 정보 - 클릭 시 상세 페이지 이동 */}
                                <button
                                    className={styles.cardBody}
                                    onClick={() => navigate(`/accommodations/${item.accommodationId}`)}
                                >
                                    <div className={styles.cardImagePlaceholder}>
                                        <span>🏠</span>
                                    </div>
                                    <div className={styles.cardInfo}>
                                        <div className={styles.categoryBadge}>{item.categoryName}</div>
                                        <h2 className={styles.accommodationName}>{item.accommodationName}</h2>
                                        <p className={styles.address}>📍 {item.address}</p>
                                        <p className={styles.checkTime}>
                                            체크인 {formatTime(item.checkInTime)} · 체크아웃 {formatTime(item.checkOutTime)}
                                        </p>
                                        <p className={styles.price}>
                                            ₩{formatPrice(item.price)} <span>/ 박</span>
                                        </p>
                                    </div>
                                </button>

                                {/* 삭제 버튼 */}
                                <button
                                    className={styles.removeBtn}
                                    onClick={() => handleRemove(item)}
                                    disabled={removingId === item.listId}
                                    title="위시리스트에서 제거"
                                >
                                    {removingId === item.listId ? '...' : '❤️'}
                                </button>
                            </li>
                        ))}
                    </ul>
                )}
            </main>
        </div>
    );
}