import type { Building } from '@/types';

interface BuildingSelectorProps {
  buildings: Building[];
  selectedId: number | null;
  onSelect: (id: number | null) => void;
}

export function BuildingSelector({ buildings, selectedId, onSelect }: BuildingSelectorProps) {
  return (
    <div className="flex gap-1 overflow-x-auto border-b border-hairline">
      <TabButton active={selectedId === null} onClick={() => onSelect(null)} label="All Buildings" />
      {buildings.map((b) => (
        <TabButton key={b.id} active={selectedId === b.id} onClick={() => onSelect(b.id)} label={b.name} />
      ))}
    </div>
  );
}

function TabButton({ active, onClick, label }: { active: boolean; onClick: () => void; label: string }) {
  return (
    <button
      onClick={onClick}
      className={`relative shrink-0 px-4 py-2.5 text-sm font-medium transition-colors ${
        active ? 'text-ink' : 'text-ink50 hover:text-ink'
      }`}
    >
      {label}
      {active && <span className="absolute left-0 right-0 -bottom-px h-0.5 bg-marigold rounded-full" />}
    </button>
  );
}
