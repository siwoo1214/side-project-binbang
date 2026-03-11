import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCategories, getTopRegions, getChildRegions, registerAccommodation, uploadAccommodationImages, searchAddress } from '../api/categoryApi';
import type { Category, RegionResponse, AccommodationRegisterRequest, AddressResponse } from '../types/category';
import Header from '../components/common/Header';
import styles from './AccommodationRegisterPage.module.css';

// 초기 폼 상태
const initialForm: AccommodationRegisterRequest = {
  name: '',
  price: 0,
  description: '',
  address: '',
  latitude: null,
  longitude: null,
  checkInTime: '15:00:00',
  checkOutTime: '11:00:00',
  categoryId: 0,
  regionName: '',
  facility: {
    bedrooms: 1,
    bathrooms: 1,
    beds: 1,
    petAllowed: false,
    parkingAvailable: false,
    hasBbq: false,
    hasWifi: false,
  },
  policy: {
    refundPolicy: '',
    houseRules: '',
    petAllowed: false,
    parkingAvailable: false,
    maxGuests: 2,
    additionalGuestFee: 0,
  },
};

export default function AccommodationRegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState<AccommodationRegisterRequest>(initialForm);
  const [categories, setCategories] = useState<Category[]>([]);
  const [topRegions, setTopRegions] = useState<RegionResponse[]>([]);
  const [childRegions, setChildRegions] = useState<RegionResponse[]>([]);
  const [selectedTopRegion, setSelectedTopRegion] = useState<number | null>(null);
  const [addressQuery, setAddressQuery] = useState('');        // 주소 검색어
  const [addressResults, setAddressResults] = useState<AddressResponse[]>([]);  // 검색 결과
  const [isSearchingAddress, setIsSearchingAddress] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState('');

  // 이미지 업로드 상태
  const [imageFiles, setImageFiles] = useState<File[]>([]);           // 선택된 파일
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);   // 미리보기 URL (blob:)

  // 카테고리 + 상위 지역 초기 로딩
  useEffect(() => {
    Promise.all([getCategories(), getTopRegions()])
      .then(([cats, regions]) => {
        setCategories(cats);
        setTopRegions(regions);
      })
      .catch(() => setServerError('기초 데이터를 불러오지 못했습니다. 새로고침 해주세요.'));
  }, []);

  // 상위 지역 선택 시 하위 지역 로딩
  const handleTopRegionChange = async (regionId: number) => {
    setSelectedTopRegion(regionId);
    setChildRegions([]);
    setForm(prev => ({ ...prev, regionName: '' }));
    if (regionId) {
      const children = await getChildRegions(regionId);
      setChildRegions(children);
    }
  };

  // 주소 검색 실행
  const handleAddressSearch = async () => {
    if (!addressQuery.trim()) return;
    setIsSearchingAddress(true);
    setAddressResults([]);
    try {
      const results = await searchAddress(addressQuery);
      setAddressResults(results);
    } catch {
      setServerError('주소 검색에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setIsSearchingAddress(false);
    }
  };

  // 검색 결과에서 주소 선택 → 폼에 자동 세팅
  const handleAddressSelect = (result: AddressResponse) => {
    setForm(prev => ({
      ...prev,
      address: result.addressName,
      latitude: result.latitude,
      longitude: result.longitude,
    }));
    setAddressResults([]);   // 결과 목록 닫기
    setAddressQuery('');
    if (errors.address) setErrors(prev => ({ ...prev, address: '' }));
  };

  // 일반 필드 변경
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  // 숫자 필드 변경
  const handleNumberChange = (name: string, value: string) => {
    setForm(prev => ({ ...prev, [name]: value === '' ? 0 : Number(value) }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  // 시설 필드 변경
  const handleFacilityChange = (name: string, value: boolean | number) => {
    setForm(prev => ({
      ...prev,
      facility: { ...prev.facility, [name]: value },
    }));
  };

  // 정책 필드 변경
  const handlePolicyChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    setForm(prev => ({
      ...prev,
      policy: {
        ...prev.policy,
        [name]: type === 'checkbox' ? checked : value,
      },
    }));
  };

  // 이미지 파일 선택 핸들러
  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    // 기존 + 새 파일 합산, 최대 10장 제한
    const combined = [...imageFiles, ...files].slice(0, 10);
    setImageFiles(combined);

    // blob URL로 미리보기 생성
    const previews = combined.map(f => URL.createObjectURL(f));
    setImagePreviews(previews);

    // input 초기화 (같은 파일 재선택 가능하게)
    e.target.value = '';
  };

  // 이미지 개별 삭제
  const handleImageRemove = (index: number) => {
    // blob URL 메모리 해제
    URL.revokeObjectURL(imagePreviews[index]);
    setImageFiles(prev => prev.filter((_, i) => i !== index));
    setImagePreviews(prev => prev.filter((_, i) => i !== index));
  };

  // 유효성 검사
  const validate = () => {
    const e: Record<string, string> = {};
    if (!form.name.trim()) e.name = '숙소명을 입력하세요';
    if (!form.price || form.price <= 0) e.price = '1박 가격을 입력하세요';
    if (!form.description.trim()) e.description = '숙소 소개를 입력하세요';
    if (!form.address.trim()) e.address = '주소를 입력하세요';
    if (!form.categoryId) e.categoryId = '카테고리를 선택하세요';
    if (!form.regionName) e.regionName = '지역을 선택하세요';
    if (!form.policy.refundPolicy.trim()) e.refundPolicy = '환불 정책을 입력하세요';
    if (!form.policy.houseRules.trim()) e.houseRules = '이용 규칙을 입력하세요';
    if (!form.policy.maxGuests || form.policy.maxGuests < 1) e.maxGuests = '최대 인원을 입력하세요';
    return e;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      // 첫 번째 에러 필드로 스크롤
      const firstKey = Object.keys(errs)[0];
      document.getElementById(firstKey)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }

    setIsLoading(true);
    setServerError('');
    try {
      // 1단계: 숙소 기본 정보 등록 → accommodationId 받기
      const result = await registerAccommodation(form);

      // 2단계: 이미지가 있으면 이어서 업로드
      // (이미지 업로드 실패해도 숙소 등록은 성공으로 처리)
      if (imageFiles.length > 0) {
        try {
          await uploadAccommodationImages(result.accommodationId, imageFiles);
        } catch {
          // 이미지 업로드 실패는 경고만 (숙소 등록 자체는 성공)
          console.warn('이미지 업로드 실패 - 숙소는 등록됨');
        }
      }

      // 등록 성공 → 홈으로 이동
      navigate('/', { state: { successMsg: `"${result.name}" 숙소가 등록됐어요!` } });
    } catch (err: any) {
      setServerError(err.response?.data?.message || '숙소 등록에 실패했습니다. 다시 시도해주세요.');
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <Header />

      <main className={styles.main}>
        <div className={styles.container}>
          {/* 페이지 타이틀 */}
          <div className={styles.pageHeader}>
            <h1 className={styles.title}>숙소 등록</h1>
            <p className={styles.subtitle}>빈방에 내 숙소를 등록해보세요</p>
          </div>

          {serverError && (
            <div className={styles.errorBanner} role="alert">⚠️ {serverError}</div>
          )}

          <form onSubmit={handleSubmit} noValidate>

            {/* ── 섹션 1: 기본 정보 ── */}
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>기본 정보</h2>

              <div className={styles.field}>
                <label htmlFor="name" className={styles.label}>숙소 이름 <span className={styles.required}>*</span></label>
                <input id="name" name="name" type="text" value={form.name}
                  onChange={handleChange} placeholder="예: 바다가 보이는 제주 펜션"
                  className={`${styles.input} ${errors.name ? styles.inputError : ''}`} />
                {errors.name && <span className={styles.fieldError}>{errors.name}</span>}
              </div>

              <div className={styles.row}>
                <div className={styles.field}>
                  <label htmlFor="categoryId" className={styles.label}>카테고리 <span className={styles.required}>*</span></label>
                  <select id="categoryId" name="categoryId"
                    value={form.categoryId || ''}
                    onChange={e => {
                      setForm(prev => ({ ...prev, categoryId: Number(e.target.value) }));
                      if (errors.categoryId) setErrors(prev => ({ ...prev, categoryId: '' }));
                    }}
                    className={`${styles.select} ${errors.categoryId ? styles.inputError : ''}`}>
                    <option value="">카테고리 선택</option>
                    {categories.map(c => (
                      <option key={c.categoryId} value={c.categoryId}>{c.name}</option>
                    ))}
                  </select>
                  {errors.categoryId && <span className={styles.fieldError}>{errors.categoryId}</span>}
                </div>

                <div className={styles.field}>
                  <label htmlFor="price" className={styles.label}>1박 가격 (원) <span className={styles.required}>*</span></label>
                  <input id="price" name="price" type="number" min="0"
                    value={form.price || ''}
                    onChange={e => handleNumberChange('price', e.target.value)}
                    placeholder="예: 150000"
                    className={`${styles.input} ${errors.price ? styles.inputError : ''}`} />
                  {errors.price && <span className={styles.fieldError}>{errors.price}</span>}
                </div>
              </div>

              <div className={styles.field}>
                <label htmlFor="description" className={styles.label}>숙소 소개 <span className={styles.required}>*</span></label>
                <textarea id="description" name="description" rows={5}
                  value={form.description} onChange={handleChange}
                  placeholder="숙소를 자유롭게 소개해주세요"
                  className={`${styles.textarea} ${errors.description ? styles.inputError : ''}`} />
                {errors.description && <span className={styles.fieldError}>{errors.description}</span>}
              </div>
            </section>

            {/* ── 섹션 2: 위치 정보 ── */}
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>위치 정보</h2>

              <div className={styles.field}>
                <label className={styles.label}>주소 <span className={styles.required}>*</span></label>

                {/* 주소 검색 입력창 */}
                <div className={styles.addressSearchRow}>
                  <input
                    type="text"
                    value={addressQuery}
                    onChange={e => setAddressQuery(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && (e.preventDefault(), handleAddressSearch())}
                    placeholder="주소를 검색하세요 (예: 제주 협재해변)"
                    className={styles.input}
                  />
                  <button
                    type="button"
                    onClick={handleAddressSearch}
                    disabled={isSearchingAddress}
                    className={styles.addressSearchBtn}
                  >
                    {isSearchingAddress ? '...' : '검색'}
                  </button>
                </div>

                {/* 검색 결과 목록 */}
                {addressResults.length > 0 && (
                  <ul className={styles.addressResults}>
                    {addressResults.map((r, i) => (
                      <li key={i}>
                        <button
                          type="button"
                          className={styles.addressResultItem}
                          onClick={() => handleAddressSelect(r)}
                        >
                          <span className={styles.addressResultName}>{r.addressName}</span>
                          <span className={styles.addressResultCoord}>
                            {r.latitude.toFixed(4)}, {r.longitude.toFixed(4)}
                          </span>
                        </button>
                      </li>
                    ))}
                  </ul>
                )}

                {/* 선택된 주소 표시 */}
                {form.address && (
                  <div className={styles.addressSelected}>
                    ✅ <strong>{form.address}</strong>
                  </div>
                )}
                {errors.address && <span className={styles.fieldError}>{errors.address}</span>}
              </div>

              {/* 지역 선택: 상위 → 하위 2단계 */}
              <div className={styles.row}>
                <div className={styles.field}>
                  <label className={styles.label}>광역시/도 <span className={styles.required}>*</span></label>
                  <select
                    value={selectedTopRegion || ''}
                    onChange={e => handleTopRegionChange(Number(e.target.value))}
                    className={styles.select}>
                    <option value="">광역시/도 선택</option>
                    {topRegions.map(r => (
                      <option key={r.regionId} value={r.regionId}>{r.name}</option>
                    ))}
                  </select>
                </div>

                <div className={styles.field}>
                  <label htmlFor="regionName" className={styles.label}>시/군/구 <span className={styles.required}>*</span></label>
                  <select id="regionName" name="regionName"
                    value={form.regionName}
                    onChange={handleChange}
                    disabled={childRegions.length === 0}
                    className={`${styles.select} ${errors.regionName ? styles.inputError : ''}`}>
                    <option value="">시/군/구 선택</option>
                    {childRegions.map(r => (
                      <option key={r.regionId} value={r.name}>{r.name}</option>
                    ))}
                  </select>
                  {errors.regionName && <span className={styles.fieldError}>{errors.regionName}</span>}
                </div>
              </div>
            </section>

            {/* ── 섹션 3: 체크인/아웃 시간 ── */}
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>체크인 / 체크아웃</h2>
              <div className={styles.row}>
                <div className={styles.field}>
                  <label htmlFor="checkInTime" className={styles.label}>체크인 시간</label>
                  <input id="checkInTime" name="checkInTime" type="time"
                    value={form.checkInTime.slice(0, 5)}
                    onChange={e => setForm(prev => ({ ...prev, checkInTime: e.target.value + ':00' }))}
                    className={styles.input} />
                </div>
                <div className={styles.field}>
                  <label htmlFor="checkOutTime" className={styles.label}>체크아웃 시간</label>
                  <input id="checkOutTime" name="checkOutTime" type="time"
                    value={form.checkOutTime.slice(0, 5)}
                    onChange={e => setForm(prev => ({ ...prev, checkOutTime: e.target.value + ':00' }))}
                    className={styles.input} />
                </div>
              </div>
            </section>

            {/* ── 섹션 4: 시설 정보 ── */}
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>시설 정보</h2>

              <div className={styles.row}>
                {[
                  { label: '침실 수', key: 'bedrooms' },
                  { label: '욕실 수', key: 'bathrooms' },
                  { label: '침대 수', key: 'beds' },
                ].map(({ label, key }) => (
                  <div key={key} className={styles.field}>
                    <label className={styles.label}>{label}</label>
                    <div className={styles.counter}>
                      <button type="button" className={styles.counterBtn}
                        onClick={() => handleFacilityChange(key, Math.max(1, (form.facility[key as keyof typeof form.facility] as number) - 1))}>
                        −
                      </button>
                      <span className={styles.counterValue}>
                        {form.facility[key as keyof typeof form.facility] as number}
                      </span>
                      <button type="button" className={styles.counterBtn}
                        onClick={() => handleFacilityChange(key, (form.facility[key as keyof typeof form.facility] as number) + 1)}>
                        +
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              <div className={styles.checkboxGrid}>
                {[
                  { label: '🐾 반려동물 허용', key: 'petAllowed' },
                  { label: '🚗 주차 가능', key: 'parkingAvailable' },
                  { label: '🍖 BBQ 가능', key: 'hasBbq' },
                  { label: '📶 와이파이', key: 'hasWifi' },
                ].map(({ label, key }) => (
                  <label key={key} className={styles.checkboxLabel}>
                    <input type="checkbox"
                      checked={form.facility[key as keyof typeof form.facility] as boolean}
                      onChange={e => handleFacilityChange(key, e.target.checked)}
                      className={styles.checkbox} />
                    {label}
                  </label>
                ))}
              </div>
            </section>

            {/* ── 섹션 5: 정책 ── */}
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>운영 정책</h2>

              <div className={styles.row}>
                <div className={styles.field}>
                  <label htmlFor="maxGuests" className={styles.label}>최대 인원 <span className={styles.required}>*</span></label>
                  <input id="maxGuests" name="maxGuests" type="number" min="1"
                    value={form.policy.maxGuests || ''}
                    onChange={e => setForm(prev => ({
                      ...prev,
                      policy: { ...prev.policy, maxGuests: Number(e.target.value) }
                    }))}
                    className={`${styles.input} ${errors.maxGuests ? styles.inputError : ''}`} />
                  {errors.maxGuests && <span className={styles.fieldError}>{errors.maxGuests}</span>}
                </div>

                <div className={styles.field}>
                  <label htmlFor="additionalGuestFee" className={styles.label}>추가 인원 요금 (원)</label>
                  <input id="additionalGuestFee" name="additionalGuestFee" type="number" min="0"
                    value={form.policy.additionalGuestFee || ''}
                    onChange={e => setForm(prev => ({
                      ...prev,
                      policy: { ...prev.policy, additionalGuestFee: Number(e.target.value) }
                    }))}
                    className={styles.input} />
                </div>
              </div>

              <div className={styles.field}>
                <label htmlFor="refundPolicy" className={styles.label}>환불 / 취소 정책 <span className={styles.required}>*</span></label>
                <textarea id="refundPolicy" name="refundPolicy" rows={3}
                  value={form.policy.refundPolicy} onChange={handlePolicyChange}
                  placeholder="예: 체크인 7일 전 취소 시 전액 환불"
                  className={`${styles.textarea} ${errors.refundPolicy ? styles.inputError : ''}`} />
                {errors.refundPolicy && <span className={styles.fieldError}>{errors.refundPolicy}</span>}
              </div>

              <div className={styles.field}>
                <label htmlFor="houseRules" className={styles.label}>이용 규칙 <span className={styles.required}>*</span></label>
                <textarea id="houseRules" name="houseRules" rows={3}
                  value={form.policy.houseRules} onChange={handlePolicyChange}
                  placeholder="예: 취사 불가, 야간 소음 금지"
                  className={`${styles.textarea} ${errors.houseRules ? styles.inputError : ''}`} />
                {errors.houseRules && <span className={styles.fieldError}>{errors.houseRules}</span>}
              </div>
            </section>

            {/* ── 섹션 6: 이미지 업로드 ── */}
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>숙소 이미지</h2>
              <p className={styles.imageHint}>
                대표 이미지(첫 번째)를 포함해 최대 10장까지 등록할 수 있어요. (선택 사항)
              </p>

              {/* 이미지 미리보기 그리드 */}
              {imagePreviews.length > 0 && (
                <div className={styles.imagePreviewGrid}>
                  {imagePreviews.map((url, idx) => (
                    <div key={idx} className={styles.imagePreviewItem}>
                      {idx === 0 && <span className={styles.representBadge}>대표</span>}
                      <img src={url} alt={`이미지 ${idx + 1}`} className={styles.previewImg} />
                      <button
                        type="button"
                        className={styles.imageRemoveBtn}
                        onClick={() => handleImageRemove(idx)}
                      >×</button>
                    </div>
                  ))}
                </div>
              )}

              {/* 이미지 추가 버튼 */}
              {imageFiles.length < 10 && (
                <label className={styles.imageUploadLabel}>
                  <input
                    type="file"
                    accept="image/*"
                    multiple
                    onChange={handleImageChange}
                    className={styles.imageInput}
                  />
                  <span className={styles.imageUploadIcon}>🖼️</span>
                  <span>이미지 추가</span>
                  <span className={styles.imageCount}>{imageFiles.length} / 10</span>
                </label>
              )}
            </section>

            {/* ── 제출 버튼 ── */}
            <div className={styles.submitWrap}>
              <button type="button" className={styles.cancelButton}
                onClick={() => navigate(-1)}>
                취소
              </button>
              <button type="submit" className={styles.submitButton} disabled={isLoading}>
                {isLoading ? <span className={styles.spinner} /> : '숙소 등록하기'}
              </button>
            </div>

          </form>
        </div>
      </main>
    </div>
  );
}
