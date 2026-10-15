import { MessageComposer } from '@/components/whatsapp/MessageComposer';

export default function BroadcastPage() {
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Broadcast</h1>
        <p className="text-sm text-ink50 mt-1">
          Send an announcement — aarti starting, a competition, a cultural program — to a whole building or everyone.
        </p>
      </div>
      <MessageComposer defaultRecipientMode="ALL" />
    </div>
  );
}
