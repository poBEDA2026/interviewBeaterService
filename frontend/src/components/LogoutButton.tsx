import { useNavigate } from 'react-router-dom';
import { Button } from './ui/button';
import { logoutAndRedirect } from '@/api/client';

export function LogoutButton() {
  const navigate = useNavigate();

  function handleClick() {
    logoutAndRedirect();
    navigate('/login');
  }

  return (
    <Button variant="secondary" onClick={handleClick}>
      Log out
    </Button>
  );
}
