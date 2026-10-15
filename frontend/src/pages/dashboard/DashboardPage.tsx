import { useEffect, useState } from 'react';
import { useBuildings } from '@/hooks/useBuildings';
import { useDashboardWidgets } from '@/hooks/useDashboardWidgets';
import { analyticsService } from '@/services/analyticsService';
import { Button } from '@/components/ui/Button';
import { StatCard } from '@/components/dashboard/StatCard';
import { BuildingSelector } from '@/components/dashboard/BuildingSelector';
import { DashboardSettingsModal } from '@/components/dashboard/DashboardSettingsModal';
import { BuildingCollectionWidget } from '@/components/dashboard/BuildingCollectionWidget';
import { FloorCollectionWidget } from '@/components/dashboard/FloorCollectionWidget';
import { FlatCollectionWidget } from '@/components/dashboard/FlatCollectionWidget';
import { TopDonorsWidget } from '@/components/dashboard/TopDonorsWidget';
import { RecentDonationsWidget } from '@/components/dashboard/RecentDonationsWidget';
import { DonationThresholdsWidget } from '@/components/dashboard/DonationThresholdsWidget';
import { WhatsAppStatisticsWidget } from '@/components/dashboard/WhatsAppStatisticsWidget';
import { formatINR } from '@/utils/currency';
import type {
  SummaryStats,
  BuildingAnalytics,
  FloorAnalytics,
  FlatAnalytics,
  TopDonor,
  Donation,
  ThresholdBucket,
  WhatsAppStats,
} from '@/types';

export default function DashboardPage() {
  const { buildings } = useBuildings(true);
  const { visibility, toggle, isVisible } = useDashboardWidgets();

  const [selectedBuildingId, setSelectedBuildingId] = useState<number | null>(null);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [loading, setLoading] = useState(true);

  const [summary, setSummary] = useState<SummaryStats | null>(null);
  const [buildingWise, setBuildingWise] = useState<BuildingAnalytics[]>([]);
  const [floorWise, setFloorWise] = useState<FloorAnalytics[]>([]);
  const [flatWise, setFlatWise] = useState<FlatAnalytics[]>([]);
  const [topDonors, setTopDonors] = useState<TopDonor[]>([]);
  const [recentDonations, setRecentDonations] = useState<Donation[]>([]);
  const [thresholds, setThresholds] = useState<ThresholdBucket[]>([]);
  const [whatsappStats, setWhatsappStats] = useState<WhatsAppStats | null>(null);

  const selectedBuilding = buildings.find((b) => b.id === selectedBuildingId) ?? null;

  useEffect(() => {
    let cancelled = false;
    setLoading(true);

    const requests: Promise<void>[] = [
      analyticsService.summary(selectedBuildingId ?? undefined).then((d) => {
        if (!cancelled) setSummary(d);
      }),
      analyticsService.topDonors(selectedBuildingId ?? undefined, 8).then((d) => {
        if (!cancelled) setTopDonors(d);
      }),
      analyticsService.recentDonations(selectedBuildingId ?? undefined, 10).then((d) => {
        if (!cancelled) setRecentDonations(d);
      }),
      analyticsService.thresholds(selectedBuildingId ?? undefined).then((d) => {
        if (!cancelled) setThresholds(d);
      }),
      analyticsService.whatsappStats(selectedBuildingId ?? undefined).then((d) => {
        if (!cancelled) setWhatsappStats(d);
      }),
    ];

    if (selectedBuildingId === null) {
      requests.push(
        analyticsService.buildingWise().then((d) => {
          if (!cancelled) setBuildingWise(d);
        })
      );
      setFloorWise([]);
      setFlatWise([]);
    } else {
      requests.push(
        analyticsService.floorWise(selectedBuildingId).then((d) => {
          if (!cancelled) setFloorWise(d);
        })
      );
      requests.push(
        analyticsService.flatWise(selectedBuildingId).then((d) => {
          if (!cancelled) setFlatWise(d);
        })
      );
    }

    Promise.all(requests).finally(() => {
      if (!cancelled) setLoading(false);
    });

    return () => {
      cancelled = true;
    };
  }, [selectedBuildingId]);

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <h1 className="text-xl sm:text-2xl font-semibold">Dashboard</h1>
          <p className="text-sm text-ink50 mt-1">
            {selectedBuilding ? selectedBuilding.name : 'All buildings'} — live collection overview
          </p>
        </div>
        <Button variant="secondary" onClick={() => setSettingsOpen(true)} className="shrink-0 self-start">
          Customize
        </Button>
      </div>

      <BuildingSelector buildings={buildings} selectedId={selectedBuildingId} onSelect={setSelectedBuildingId} />

      {loading && !summary ? (
        <p className="text-sm text-ink50 py-8 text-center">Loading dashboard…</p>
      ) : (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {isVisible('totalDonation') && (
              <StatCard label="Total donation" value={formatINR(summary?.totalDonation ?? 0)} tone="marigold" />
            )}
            {isVisible('totalContributors') && (
              <StatCard label="Total donors" value={String(summary?.totalDonors ?? 0)} />
            )}
            {isVisible('averageDonation') && (
              <StatCard label="Average donation" value={formatINR(summary?.averageDonation ?? 0)} />
            )}
            {isVisible('highestDonation') && (
              <StatCard label="Highest donation" value={formatINR(summary?.highestDonation ?? 0)} tone="moss" />
            )}
            {isVisible('lowestDonation') && (
              <StatCard label="Lowest donation" value={formatINR(summary?.lowestDonation ?? 0)} />
            )}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
            {isVisible('buildingCollection') && selectedBuildingId === null && (
              <BuildingCollectionWidget data={buildingWise} />
            )}

            {isVisible('floorCollection') && selectedBuilding?.requiresFloor && (
              <FloorCollectionWidget data={floorWise} />
            )}

            {isVisible('flatCollection') && selectedBuildingId !== null && <FlatCollectionWidget data={flatWise} />}

            {isVisible('topDonors') && <TopDonorsWidget data={topDonors} />}

            {isVisible('recentDonations') && <RecentDonationsWidget data={recentDonations} />}

            {isVisible('donationThresholds') && <DonationThresholdsWidget data={thresholds} />}

            {isVisible('whatsappStatistics') && whatsappStats && (
              <WhatsAppStatisticsWidget data={whatsappStats} />
            )}
          </div>
        </>
      )}

      <DashboardSettingsModal
        open={settingsOpen}
        onClose={() => setSettingsOpen(false)}
        visibility={visibility}
        onToggle={toggle}
      />
    </div>
  );
}
