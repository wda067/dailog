import { defineStore } from 'pinia';
import { v4 as uuidv4 } from 'uuid';

interface Alert {
  id: string;
  message: string;
  type: string;
}

export const useAlertStore = defineStore('alert', {
  state: () => ({
    alerts: [] as Alert[],
  }),
  actions: {
    vAlert(message: string, type: string = 'error') {
      const id = uuidv4();
      this.alerts.push({ id, message, type });
      setTimeout(() => {
        this.alerts = this.alerts.filter(alert => alert.id !== id);
      }, 1500);
    },
    vSuccess(message: string) {
      this.vAlert(message, 'success');
    },
  },
});
