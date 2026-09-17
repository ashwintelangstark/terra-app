import { createFileRoute, redirect } from "@tanstack/react-router";

export const Route = createFileRoute("/_authenticated/cancellations")({
  beforeLoad: () => {
    throw redirect({ to: "/bookings", replace: true });
  },
  component: () => null,
});
