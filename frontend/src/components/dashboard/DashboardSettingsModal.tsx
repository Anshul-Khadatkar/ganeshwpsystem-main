import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { WIDGET_DEFINITIONS, type WidgetKey } from '@/hooks/useDashboardWidgets';

interface DashboardSettingsModalProps {
  open: boolean;
  onClose: () => void;
  visibility: Record<WidgetKey, boolean>;
  onToggle: (key: WidgetKey) => void;
}

export function DashboardSettingsModal({ open, onClose, visibility, onToggle }: DashboardSettingsModalProps) {
  return (
    <Modal open={open} onClose={onClose} title="Dashboard settings">
      <p className="text-sm text-ink50 mb-4">Choose what shows up on your dashboard. Changes apply immediately.</p>
      <div className="flex flex-col gap-1">
        {WIDGET_DEFINITIONS.map((widget) => (
          <label
            key={widget.key}
            className="flex items-center gap-3 px-2 py-2 rounded-md hover:bg-paper text-sm text-ink cursor-pointer"
          >
            <input
              type="checkbox"
              checked={visibility[widget.key]}
              onChange={() => onToggle(widget.key)}
              className="rounded border-hairline text-ink focus-visible:ring-2 focus-visible:ring-marigold"
            />
            {widget.label}
          </label>
        ))}
      </div>
      <div className="flex justify-end pt-4 mt-2 border-t border-hairline">
        <Button onClick={onClose}>Done</Button>
      </div>
    </Modal>
  );
}
