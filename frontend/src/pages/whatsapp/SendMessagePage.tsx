import { MessageComposer } from '@/components/whatsapp/MessageComposer';

export default function SendMessagePage() {
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Send message</h1>
        <p className="text-sm text-ink50 mt-1">
          Write anything — an update, a reminder, a personal note — and choose exactly who gets it.
        </p>
      </div>
      <MessageComposer defaultRecipientMode="EXPLICIT" />
    </div>
  );
}
