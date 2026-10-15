import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from '@/hooks/useAuth';
import { RequireAuth } from '@/components/RequireAuth';
import { AdminLayout } from '@/layouts/AdminLayout';
import LoginPage from '@/pages/auth/LoginPage';
import DashboardPage from '@/pages/dashboard/DashboardPage';
import BuildingsPage from '@/pages/buildings/BuildingsPage';
import DonationsPage from '@/pages/donations/DonationsPage';
import AddDonationPage from '@/pages/donations/AddDonationPage';
import SendMessagePage from '@/pages/whatsapp/SendMessagePage';
import BroadcastPage from '@/pages/whatsapp/BroadcastPage';
import TemplatesPage from '@/pages/whatsapp/TemplatesPage';
import BroadcastHistoryPage from '@/pages/whatsapp/BroadcastHistoryPage';
import BroadcastDetailPage from '@/pages/whatsapp/BroadcastDetailPage';
import SettingsPage from '@/pages/settings/SettingsPage';
import DebugController4719Page from '@/pages/debug/DebugController4719Page';
import { ComingSoonPage } from '@/pages/ComingSoonPage';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route element={<RequireAuth />}>
          <Route element={<AdminLayout />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/buildings" element={<BuildingsPage />} />
            <Route path="/donations" element={<DonationsPage />} />
            <Route path="/donations/new" element={<AddDonationPage />} />
            <Route
              path="/analytics"
              element={
                <ComingSoonPage
                  title="Analytics"
                  description="A dedicated deep-dive analytics page — the Dashboard already covers building/floor/flat breakdowns; this page adds trend-over-time views in a later pass."
                />
              }
            />
            <Route path="/whatsapp/send" element={<SendMessagePage />} />
            <Route path="/whatsapp/broadcast" element={<BroadcastPage />} />
            <Route path="/whatsapp/templates" element={<TemplatesPage />} />
            <Route path="/whatsapp/history" element={<BroadcastHistoryPage />} />
            <Route path="/whatsapp/history/:id" element={<BroadcastDetailPage />} />
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="/debug-controller-4719" element={<DebugController4719Page />} />
          </Route>
        </Route>
      </Routes>
    </AuthProvider>
  );
}
