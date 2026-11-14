import { useEffect, useState } from 'react';
import type { DriverProfile, Profile } from '../api/types';
import { useAuth } from '../auth/useAuth';
import { Alert } from '../components/Alert';
import { Spinner } from '../components/Spinner';
import { profileService } from '../services/profileService';

export function DashboardPage() {
  const { role } = useAuth();

  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!role) return;

    let cancelled = false; // guards against setting state after unmount

    async function load() {
      setLoading(true);
      setError(null);
      try {
        const data = await profileService.getMyProfile(role!);
        if (!cancelled) setProfile(data);
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Could not load profile.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [role]);

  if (loading) return <Spinner label="Loading your profile…" />;
  if (error) return <Alert kind="error" message={error} />;
  if (!profile) return null;

  const isDriver = role === 'DRIVER';
  const driver = isDriver ? (profile as DriverProfile) : null;

  return (
    <section className="dashboard">
      <div className="dashboard__greeting">
        <h1>Hello, {profile.name.split(' ')[0]} 👋</h1>
        <p>Here is your account overview.</p>
      </div>

      <div className="profile-card">
        <div className="profile-card__avatar" aria-hidden="true">
          {initials(profile.name)}
        </div>

        <dl className="profile-card__details">
          <ProfileRow label="Name" value={profile.name} />
          <ProfileRow label="Email" value={profile.email} />
          <ProfileRow label="Phone" value={profile.phone} />
          <ProfileRow label="Account type" value={role === 'DRIVER' ? 'Driver' : 'Rider'} />
          {driver && (
            <ProfileRow
              label="Availability"
              value={driver.available ? 'Available' : 'Unavailable'}
            />
          )}
          <ProfileRow label="Profile ID" value={String(profile.id)} />
        </dl>
      </div>
    </section>
  );
}

function ProfileRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="profile-card__row">
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}

function initials(fullName: string): string {
  return fullName
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('');
}
