import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import type { Role } from '../api/types';
import { useAuth } from '../auth/useAuth';
import { Alert } from '../components/Alert';
import { Button } from '../components/Button';
import { TextField } from '../components/TextField';

interface FormValues {
  name: string;
  email: string;
  phone: string;
  password: string;
  role: Role;
}

const EMPTY_FORM: FormValues = {
  name: '',
  email: '',
  phone: '',
  password: '',
  role: 'RIDER',
};

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState<FormValues>(EMPTY_FORM);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function updateField<K extends keyof FormValues>(key: K, value: FormValues[K]) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    if (form.password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }

    setSubmitting(true);
    try {
      await register(form.role, {
        email: form.email,
        password: form.password,
        name: form.name,
        phone: form.phone,
      });
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-card">
      <h1>Create your account</h1>
      <p className="auth-card__subtitle">Join RideHailing as a rider or a driver.</p>

      <form onSubmit={handleSubmit} noValidate>
        <Alert kind="error" message={error} />

        <div className="field">
          <span className="field__label">I am a</span>
          <div className="role-toggle">
            <label>
              <input
                type="radio"
                name="role"
                value="RIDER"
                checked={form.role === 'RIDER'}
                onChange={() => updateField('role', 'RIDER')}
              />
              Rider
            </label>
            <label>
              <input
                type="radio"
                name="role"
                value="DRIVER"
                checked={form.role === 'DRIVER'}
                onChange={() => updateField('role', 'DRIVER')}
              />
              Driver
            </label>
          </div>
        </div>

        <TextField
          id="name"
          label="Full name"
          autoComplete="name"
          value={form.name}
          onChange={(e) => updateField('name', e.target.value)}
          required
        />
        <TextField
          id="email"
          label="Email"
          type="email"
          autoComplete="email"
          value={form.email}
          onChange={(e) => updateField('email', e.target.value)}
          required
        />
        <TextField
          id="phone"
          label="Phone"
          autoComplete="tel"
          value={form.phone}
          onChange={(e) => updateField('phone', e.target.value)}
          required
        />
        <TextField
          id="password"
          label="Password (min 8 characters)"
          type="password"
          autoComplete="new-password"
          value={form.password}
          onChange={(e) => updateField('password', e.target.value)}
          required
        />

        <Button type="submit" loading={submitting}>
          Create account
        </Button>
      </form>

      <p className="auth-card__footer">
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </div>
  );
}
