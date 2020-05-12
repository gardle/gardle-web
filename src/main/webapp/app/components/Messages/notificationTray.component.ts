import Vue from 'vue';
import { Component, Inject } from 'vue-property-decorator';
import NotificationTrayItem from '@/components/Messages/notificationTrayItem.vue';
import MessageService from '@/shared/services/Message/message.service';
import { Message, MessageType } from '@/shared/model/message.model';
import { EventBus } from '@/shared/eventbus/eventbus';

@Component({
  components: {
    NotificationTrayItem
  }
})
export default class NotificationTrayComponent extends Vue {
  @Inject('messageService')
  private messageService: () => MessageService;
  private notificationList: Message[] = [];
  open = false;
  pollInterval = null;
  nrOfUnreadMessages = 0;

  public getNotifications() {
    if (this.$store.getters.loggedIn) {
      this.messageService()
        .getUnreadNotifications()
        .then(res => {
          if (this.notificationList.length !== 0) {
            const latestMsgDate: Date = new Date(this.notificationList[0].createdDate);
            // Extract list of threads which have unread user messages to avoid double notifications
            const existingUnreadMessageNotifications: string[] = this.notificationList
              .filter(notif => notif.type === MessageType.DEFAULT)
              .map(notif => notif.thread);
            this.notificationList = this.getMessagesNewerThanDate(latestMsgDate, res, existingUnreadMessageNotifications).concat(
              this.notificationList
            );
          } else {
            this.notificationList = res;
          }
          const newNrOfUnreadMessages = this.notificationList.filter(notification => !notification.opened).length;
          this.notifyMessagesPage(newNrOfUnreadMessages);
          this.nrOfUnreadMessages = newNrOfUnreadMessages;
        });
    }
  }

  private getMessagesNewerThanDate(lastDate: Date, toBeFiltered: Message[], existingUnread: string[]): Message[] {
    const newMessages: Message[] = [];
    for (const msg of toBeFiltered) {
      if (new Date(msg.createdDate) > lastDate) {
        if (msg.type === MessageType.DEFAULT && !existingUnread.includes(msg.thread)) {
          newMessages.push(msg);
        } else if (msg.type !== MessageType.DEFAULT) {
          newMessages.push(msg);
        }
      } else {
        break;
      }
    }
    return newMessages;
  }

  private setLocalSystemNotificationsToOpened() {
    this.notificationList.forEach(notif => {
      if (notif.type !== MessageType.DEFAULT) {
        notif.opened = true;
      }
    });
  }

  public notificationsExist() {
    return this.notificationList === undefined || this.notificationList.length === 0;
  }

  public closeTray() {
    this.open = false;
  }

  public opened() {
    this.messageService()
      .markSystemMessagesOpened()
      .then(res => {
        this.setLocalSystemNotificationsToOpened();
        const newNrOfUnreadMsgs = this.notificationList.filter(notification => !notification.opened).length;
        this.notifyMessagesPage(newNrOfUnreadMsgs);
        this.nrOfUnreadMessages = newNrOfUnreadMsgs;
      });
  }

  private removeByThread(thread: string) {
    this.notificationList = this.notificationList.filter(notification => notification.thread !== thread);
    this.setLocalSystemNotificationsToOpened();
    const newNrOfUnreadMsgs = this.notificationList.filter(notification => !notification.opened).length;
    this.notifyMessagesPage(newNrOfUnreadMsgs);
    this.nrOfUnreadMessages = newNrOfUnreadMsgs;
  }

  private notifyMessagesPage(newNrOfUnreadMessages: number) {
    if (this.$route.name === 'Account:Messages' && this.nrOfUnreadMessages !== newNrOfUnreadMessages) {
      EventBus.$emit('refresh-messages');
    }
  }

  mounted() {
    this.getNotifications();
  }

  created() {
    EventBus.$on('thread-opened', args => this.removeByThread(args.thread));

    this.pollInterval = setInterval(
      function() {
        this.getNotifications();
      }.bind(this),
      10000
    );
  }
}
