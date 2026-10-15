interface ComingSoonProps {
  title: string;
  description: string;
}

export function ComingSoonPage({ title, description }: ComingSoonProps) {
  return (
    <div className="flex flex-col gap-2">
      <h1 className="text-2xl font-semibold">{title}</h1>
      <p className="text-sm text-ink50 max-w-md">{description}</p>
      <div className="mt-6 rounded-card border border-dashed border-hairline p-10 text-center text-sm text-ink30">
        This page is built in a later session.
      </div>
    </div>
  );
}
