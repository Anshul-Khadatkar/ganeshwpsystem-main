import { useState } from 'react';
import { useBuildings } from '@/hooks/useBuildings';
import { buildingService } from '@/services/buildingService';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { EmptyState } from '@/components/ui/EmptyState';
import { BuildingForm } from './BuildingForm';
import type { Building, BuildingRequest } from '@/types';

export default function BuildingsPage() {
  const { buildings, loading, error, reload } = useBuildings(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Building | null>(null);

  const openCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };

  const openEdit = (building: Building) => {
    setEditing(building);
    setModalOpen(true);
  };

  const handleSubmit = async (request: BuildingRequest) => {
    if (editing) {
      await buildingService.update(editing.id, request);
    } else {
      await buildingService.create(request);
    }
    setModalOpen(false);
    await reload();
  };

  const toggleActive = async (building: Building) => {
    await buildingService.setActive(building.id, !building.active);
    await reload();
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <h1 className="text-xl sm:text-2xl font-semibold">Buildings</h1>
          <p className="text-sm text-ink50 mt-1">
            Every donation category — towers, Row Houses, Shops, or anything you add later.
          </p>
        </div>
        <Button onClick={openCreate} className="shrink-0 self-start">+ Add building</Button>
      </div>

      {error && (
        <p className="text-sm text-vermilion bg-vermilion/5 border border-vermilion/20 rounded-md px-3 py-2">
          {error}
        </p>
      )}

      <Card className="overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-sm text-ink50">Loading buildings…</div>
        ) : buildings.length === 0 ? (
          <EmptyState
            title="No buildings yet"
            description="Add your first donation category to start recording donations."
            action={<Button onClick={openCreate}>+ Add building</Button>}
          />
        ) : (
          <div className="table-scroll">
          <table>
            <thead>
              <tr className="border-b border-hairline text-left text-xs uppercase tracking-wide text-ink50">
                <th className="px-4 py-3 font-medium">Name</th>
                <th className="px-4 py-3 font-medium">Description</th>
                <th className="px-4 py-3 font-medium">Floor / Flat</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {buildings.map((b) => (
                <tr key={b.id} className="border-b border-hairline last:border-0 hover:bg-paper/60">
                  <td className="px-4 py-3 font-medium text-ink">{b.name}</td>
                  <td className="px-4 py-3 text-ink50">{b.description || '—'}</td>
                  <td className="px-4 py-3 text-ink50">
                    {[b.requiresFloor && 'Floor', b.requiresFlat && 'Flat'].filter(Boolean).join(' / ') || 'Not applicable'}
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge active={b.active} />
                  </td>
                  <td className="px-4 py-3 text-right space-x-2">
                    <Button variant="ghost" size="sm" onClick={() => openEdit(b)}>
                      Edit
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => toggleActive(b)}>
                      {b.active ? 'Deactivate' : 'Activate'}
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          </div>
        )}
      </Card>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit building' : 'Add building'}>
        <BuildingForm initial={editing} onSubmit={handleSubmit} onCancel={() => setModalOpen(false)} />
      </Modal>
    </div>
  );
}
