import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import HomePage from './pages/HomePage';
import AccommodationRegisterPage from './pages/AccommodationRegisterPage';
import AccommodationDetailPage from './pages/AccommodationDetailPage';
import ChatListPage from './pages/ChatListPage';
import ChatRoomPage from './pages/ChatRoomPage';
import OAuth2RedirectPage from './pages/OAuth2RedirectPage';
import MyReservationsPage from './pages/MyReservationsPage';
import MyAccommodationsPage from './pages/MyAccommodationsPage';
import WishlistPage from './pages/WishlistPage';

// 로그인이 필요한 라우트 보호
const PrivateRoute = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = !!localStorage.getItem('accessToken');
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 퍼블릭 */}
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/oauth2/redirect" element={<OAuth2RedirectPage />} />
        <Route path="/accommodations/:id" element={<AccommodationDetailPage />} />

        {/* 프라이빗 (로그인 필요) */}
        <Route path="/accommodations/register" element={
          <PrivateRoute><AccommodationRegisterPage /></PrivateRoute>
        } />
        <Route path="/accommodations/my" element={
          <PrivateRoute><MyAccommodationsPage /></PrivateRoute>
        } />
        <Route path="/reservations/my" element={
          <PrivateRoute><MyReservationsPage /></PrivateRoute>
        } />
        <Route path="/wishlist" element={
          <PrivateRoute><WishlistPage /></PrivateRoute>
        } />
        <Route path="/chat" element={
          <PrivateRoute><ChatListPage /></PrivateRoute>
        } />
        <Route path="/chat/:roomId" element={
          <PrivateRoute><ChatRoomPage /></PrivateRoute>
        } />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
