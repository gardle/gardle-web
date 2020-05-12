import { EventBus } from '@/shared/eventbus/eventbus';

/*  A notification item can have following attributes
 *       {
 *           text: 'localised message',
 *           color: 'vuetify color name',
 *           primaryAction: {
 *               icon: 'icon name',
 *               callback: function
 *           },
 *           action: {
 *               icon: 'icon name',
 *               callback: 'function'
 *           }
 *           close: false, // would apply a close icon and a close callback, default false
 *           timeout: 2000 // time in ms, default is 2000 ms,
 *           multiline: false
 *       }
 * */

export const snackbarQueue = {
  data: () => ({
    notificationItem: {},
    notificationQueue: [],
    notifications: false
  }),
  computed: {
    hasNotificationsPending() {
      return this.notificationQueue.length > 0;
    },
    notificationAction() {
      return this.notificationItem.action !== undefined
        ? {
            icon: this.notificationItem.action.icon,
            callback:
              this.notificationItem.action.callback !== undefined
                ? () => {
                    const ret = this.notificationItem.action.callback();
                    this.notifications = !ret;
                  }
                : () => {}
          }
        : this.notificationItem.close === true
        ? {
            icon: 'mdi-close',
            callback: () => {
              this.notifications = false;
            }
          }
        : null;
    },
    notificationText() {
      return this.notificationItem.text;
    },
    notificationColor() {
      return this.notificationItem.color ? this.notificationItem.color : 'info';
    },
    notificationPrimaryAction() {
      return this.notificationItem.primaryAction !== undefined
        ? {
            icon: this.notificationItem.primaryAction.icon,
            callback: () => {
              const ret = this.notificationItem.primaryAction.callback();
              this.notifications = !ret;
            }
          }
        : null;
    },
    notificationTimeout() {
      return this.notificationItem.timeout !== undefined ? this.notificationItem.timeout : 2000;
    },
    notificationMultiline() {
      return this.notificationItem.multiline !== undefined ? this.notificationItem.multiline : false;
    },
    notificationClose() {
      return this.notificationItem.close;
    }
  },
  watch: {
    notifications() {
      if (!this.notifications && this.hasNotificationsPending) {
        this.notificationItem = this.notificationQueue.shift();
        this.$nextTick(() => (this.notifications = true));
      }
    }
  },
  created() {
    EventBus.$on('add-notification', this.addNotification);
  },
  beforeDestroy() {
    EventBus.$off('add-notification');
  },
  methods: {
    addNotification(item) {
      this.notificationQueue.push(item);
      if (!this.notifications) {
        this.notificationItem = this.notificationQueue.shift();
        this.notifications = true;
      }
    }
  }
};
