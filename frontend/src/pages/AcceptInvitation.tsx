import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { CheckCircle, XCircle, Loader2 } from 'lucide-react';
import { teamService } from '../services/teamService';

/**
 * Page for accepting project invitations via email link.
 */
export function AcceptInvitation() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [projectName, setProjectName] = useState('');

  const token = searchParams.get('token');

  const acceptMutation = useMutation({
    mutationFn: (token: string) => teamService.acceptInvitation(token),
    onSuccess: (data) => {
      setStatus('success');
      // Redirect to project board after 2 seconds
      setTimeout(() => {
        navigate('/dashboard');
      }, 2000);
    },
    onError: (error: any) => {
      setStatus('error');
      setErrorMessage(error.response?.data?.message || 'Failed to accept invitation');
    },
  });

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setErrorMessage('Invalid invitation link');
      return;
    }

    // Check if user is logged in
    const authToken = localStorage.getItem('token');
    if (!authToken) {
      // Redirect to login with return URL
      navigate(`/login?redirect=/accept-invitation?token=${token}`);
      return;
    }

    // Accept invitation
    acceptMutation.mutate(token);
  }, [token]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-primary-100 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full">
        {status === 'loading' && (
          <div className="text-center">
            <Loader2 className="w-16 h-16 text-primary-600 animate-spin mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              Accepting Invitation
            </h2>
            <p className="text-gray-600">
              Please wait while we process your invitation...
            </p>
          </div>
        )}

        {status === 'success' && (
          <div className="text-center">
            <CheckCircle className="w-16 h-16 text-green-600 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              Welcome to the Team!
            </h2>
            <p className="text-gray-600 mb-4">
              You have successfully joined the project.
            </p>
            <p className="text-sm text-gray-500">
              Redirecting to dashboard...
            </p>
          </div>
        )}

        {status === 'error' && (
          <div className="text-center">
            <XCircle className="w-16 h-16 text-red-600 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              Invitation Failed
            </h2>
            <p className="text-gray-600 mb-6">
              {errorMessage}
            </p>
            <button
              onClick={() => navigate('/dashboard')}
              className="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
            >
              Go to Dashboard
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

