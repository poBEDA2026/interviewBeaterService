import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthBootstrapper } from '@/auth/AuthBootstrapper';
import { ProtectedRoute } from '@/auth/ProtectedRoute';
import { LoginPage } from '@/pages/LoginPage';
import { SignupPage } from '@/pages/SignupPage';
import { HomePage } from '@/pages/HomePage';

export function App() {
  return (
    <BrowserRouter>
      <AuthBootstrapper>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <HomePage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<LoginPage />} />
        </Routes>
      </AuthBootstrapper>
    </BrowserRouter>
  );
}
