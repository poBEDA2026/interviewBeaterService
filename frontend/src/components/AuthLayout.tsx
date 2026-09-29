import { ReactNode } from 'react';

export function AuthLayout({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle?: string;
  children: ReactNode;
}) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 px-4">
      <div className="mb-6 text-xl font-semibold text-gray-900">InterviewBeater</div>
      <div className="w-full max-w-sm rounded-lg border border-gray-200 bg-white p-8 shadow-sm">
        <h1 className="mb-1 text-2xl font-semibold text-gray-900">{title}</h1>
        {subtitle && <p className="mb-6 text-sm text-gray-500">{subtitle}</p>}
        {children}
      </div>
    </div>
  );
}
