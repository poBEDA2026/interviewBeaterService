import { useNavigate } from 'react-router-dom';
import { LogoutButton } from '@/components/LogoutButton';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { useAuth } from '@/auth/useAuth';

export function AccountPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const fullName = user?.fullName ?? '—';
  const email = user?.email ?? '—';
  const role = user ? user.role.replace(/^ROLE_/, '') : '—';

  return (
    <div className="mx-auto max-w-2xl px-4 py-10">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-gray-900">Account</h1>
        <LogoutButton />
      </div>
      <Card className="space-y-6">
        <div className="space-y-1">
          <p className="text-sm font-medium text-gray-500">Full name</p>
          <p className="text-lg text-gray-900">{fullName}</p>
        </div>
        <div className="space-y-1">
          <p className="text-sm font-medium text-gray-500">Email</p>
          <p className="text-lg text-gray-900">{email}</p>
        </div>
        <div className="space-y-1">
          <p className="text-sm font-medium text-gray-500">Role</p>
          <p className="text-lg text-gray-900">{role}</p>
        </div>
        <Button onClick={() => navigate('/account/stats')}>Show statistics</Button>
      </Card>
    </div>
  );
}
