import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { ForgotPassword } from './pages/ForgotPassword';
import { ResetPassword } from './pages/ResetPassword';
import { Dashboard } from './pages/Dashboard';
import { Board } from './pages/Board';
import { Backlog } from './pages/Backlog';
import { ProjectSettings } from './pages/ProjectSettings';
import { TeamManagement } from './pages/TeamManagement';
import { AcceptInvitation } from './pages/AcceptInvitation';
import { Settings } from './pages/Settings';
import { MyIssues } from './pages/MyIssues';
import { authService } from './services/authService';

const queryClient = new QueryClient();

function PrivateRoute({ children }: { children: React.ReactNode }) {
  return authService.isAuthenticated() ? <>{children}</> : <Navigate to="/login" />;
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/accept-invitation" element={<AcceptInvitation />} />
          <Route
            path="/dashboard"
            element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            }
          />
          <Route
            path="/my-issues"
            element={
              <PrivateRoute>
                <MyIssues />
              </PrivateRoute>
            }
          />
          <Route
            path="/settings"
            element={
              <PrivateRoute>
                <Settings />
              </PrivateRoute>
            }
          />
          <Route
            path="/projects/:projectId/board"
            element={
              <PrivateRoute>
                <Board />
              </PrivateRoute>
            }
          />
          <Route
            path="/projects/:projectId/backlog"
            element={
              <PrivateRoute>
                <Backlog />
              </PrivateRoute>
            }
          />
          <Route
            path="/projects/:projectId/settings"
            element={
              <PrivateRoute>
                <ProjectSettings />
              </PrivateRoute>
            }
          />
          <Route
            path="/projects/:projectId/team"
            element={
              <PrivateRoute>
                <TeamManagement />
              </PrivateRoute>
            }
          />
          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
