import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/common/Header';
import { getMyReservations, cancelReservation } from '../api/reservationApi';
import type { ReservationListResponse, ReservationStatus } from '../types/reservation';
import styles from './MyReservationsPage.module.css';

// 상태 탭 정의
const TABS: { label: string; value: ReservationStatus | 'ALL' }[] = [
    { label: '전체', value: 'ALL' },
    { label: '예약 확정', value: 'RESERVED' },
    { label: '완료', value: 'COMPLETED' },
    { label: '취소됨', value: 'CANCELLED' },
];

// 상태 뱃지 스타일 매핑
const STATUS_LABEL: Record<ReservationStatus, string> = {
    RESERVED: '예약 확정',
    COMPLETED: '완료',
    CANCELLED: '취소됨',
};

// 날짜 포맷 (2026-03-10 → 2026.03.10)
const formatDate = (date: string) => date.replace(/-/g, '.');

// 가격 포맷
const formatPrice = (price: number) => price.toLocaleString('ko-KR');

export default function MyReservationsPage() {
    const navigate = useNavigate();

    const [reservations, setReservations] = useState<ReservationListResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState<ReservationStatus | 'ALL'>('ALL');
    const [cancellingId, setCancellingId] = useState<number | null>(null); // 취소 처리 중인 예약 ID

    // ── 예약 목록 로드 ──
    useEffect(() => {
        const fetch = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await getMyReservations(
                    activeTab === 'ALL' ? undefined : activeTab
                );
                setReservations(data);
            } catch {
                setError('예약 목록을 불러오지 못했습니다.');
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, [activeTab]); // 탭 바뀔 때마다 재조회

    // ── 예약 취소 ──
    const handleCancel = async (reservationId: number) => {
        const confirmed = window.confirm('예약을 취소하시겠습니까?');
        if (!confirmed) return;

        try {
            setCancellingId(reservationId);
            await cancelReservation(reservationId);
            // 성공 시 해당 항목 상태를 CANCELLED로 변경 (서버 재조회 없이)
            setReservations(prev =>
                prev.map(r =>
                    r.reservationId === reservationId
                        ? { ...r, status: 'CANCELLED' as ReservationStatus }
                        : r
                )
            );
        } catch (err: any) {
            const msg = err.response?.data?.message || '예약 취소 중 오류가 발생했습니다.';
            alert(msg);
        } finally {
            setCancellingId(null);
        }
    };

    return (
        <div className={styles.page}>
            <Header />
            <main className={styles.main}>
                <h1 className={styles.title}>내 예약</h1>

                {/* ── 상태 탭 ── */}
                <div className={styles.tabs}>
                    {TABS.map(tab => (
                        <button
                            key={tab.value}
                            className={`${styles.tab} ${activeTab === tab.value ? styles.tabActive : ''}`}
                            onClick={() => setActiveTab(tab.value)}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>

                {/* ── 로딩 ── */}
                {loading && (
                    <div className={styles.skeletonList}>
                        {Array.from({ length: 3 }).map((_, i) => (
                            <div key={i} className={styles.skeletonItem}>
                                <div className={styles.skeletonContent}>
                                    <div className={styles.skeletonLine} style={{ width: '50%' }} />
                                    <div className={styles.skeletonLine} style={{ width: '70%' }} />
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
                {!loading && !error && reservations.length === 0 && (
                    <div className={styles.empty}>
                        <p className={styles.emptyIcon}>📋</p>
                        <p className={styles.emptyText}>예약 내역이 없어요</p>
                        <p className={styles.emptySub}>마음에 드는 숙소를 찾아 예약해보세요</p>
                        <button className={styles.exploreBtn} onClick={() => navigate('/')}>
                            숙소 둘러보기
                        </button>
                    </div>
                )}

                {/* ── 예약 카드 목록 ── */}
                {!loading && !error && reservations.length > 0 && (
                    <ul className={styles.list}>
                        {reservations.map(reservation => (
                            <li key={reservation.reservationId} className={styles.card}>

                                {/* 상단: 상태 뱃지 + 예약번호 */}
                                <div className={styles.cardTop}>
                                    <span className={`${styles.statusBadge} ${styles[`status${reservation.status}`]}`}>
                                        {STATUS_LABEL[reservation.status]}
                                    </span>
                                    <span className={styles.reservationId}>
                                        #{reservation.reservationId}
                                    </span>
                                </div>

                                {/* 숙소명 - 클릭 시 상세 페이지 이동 */}
                                <button
                                    className={styles.accommodationName}
                                    onClick={() => navigate(`/accommodations/${reservation.accommodationId}`)}
                                >
                                    🏠 {reservation.accommodationName}
                                </button>

                                {/* 예약 정보 */}
                                <div className={styles.infoGrid}>
                                    <div className={styles.infoItem}>
                                        <span className={styles.infoLabel}>체크인</span>
                                        <span className={styles.infoValue}>{formatDate(reservation.checkInDate)}</span>
                                    </div>
                                    <div className={styles.infoItem}>
                                        <span className={styles.infoLabel}>체크아웃</span>
                                        <span className={styles.infoValue}>{formatDate(reservation.checkOutDate)}</span>
                                    </div>
                                    <div className={styles.infoItem}>
                                        <span className={styles.infoLabel}>인원</span>
                                        <span className={styles.infoValue}>{reservation.personnel}명</span>
                                    </div>
                                    <div className={styles.infoItem}>
                                        <span className={styles.infoLabel}>호스트</span>
                                        <span className={styles.infoValue}>{reservation.hostName}</span>
                                    </div>
                                </div>

                                {/* 하단: 금액 + 취소 버튼 */}
                                <div className={styles.cardBottom}>
                                    <span className={styles.totalPrice}>
                                        ₩{formatPrice(reservation.totalPrice)}
                                    </span>
                                    {reservation.status === 'RESERVED' && (
                                        <button
                                            className={styles.cancelBtn}
                                            onClick={() => handleCancel(reservation.reservationId)}
                                            disabled={cancellingId === reservation.reservationId}
                                        >
                                            {cancellingId === reservation.reservationId ? '취소 중...' : '예약 취소'}
                                        </button>
                                    )}
                                </div>

                            </li>
                        ))}
                    </ul>
                )}
            </main>
        </div>
    );
}