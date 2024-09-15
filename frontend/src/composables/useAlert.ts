import { storeToRefs } from 'pinia';
import { useAlertStore } from '@/stores/alert';

export interface Alert {
  id: string;
  message: string;
  type: string;
}

export function useAlert() {
  const { alerts } = storeToRefs(useAlertStore());
  const { vAlert, vSuccess } = useAlertStore();
  return {
    alerts,
    vAlert,
    vSuccess,
  };
}
