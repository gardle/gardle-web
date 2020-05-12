import Vue from 'vue';
import { Component, Inject, Prop } from 'vue-property-decorator';
import { Message, MessageType } from '@/shared/model/message.model';
import MessageService from '@/shared/services/Message/message.service';
import { EventBus } from '@/shared/eventbus/eventbus';

@Component
export default class NotificationTrayItemComponent extends Vue {
  @Prop({ type: Object, required: true }) notification: Message;
  userType = MessageType.DEFAULT;
  leasingOpenType = MessageType.SYSTEM_LEASING_OPEN;
  leasingCancelledType = MessageType.SYSTEM_LEASING_CANCELLED;
  leasingReservedType = MessageType.SYSTEM_LEASING_RESERVED;
  leasingRejectedType = MessageType.SYSTEM_LEASING_REJECTED;
  @Inject('messageService')
  private messageService: () => MessageService;
  date_time_format = 'yyyy-MM-dd HH:mm';

  public handleXClick() {
    switch (this.notification.type) {
      case MessageType.DEFAULT:
        this.messageService()
          .markThreadOpened(this.notification.thread)
          .then(res => {
            EventBus.$emit('thread-opened', { thread: this.notification.thread });
          });
        break;
      case MessageType.SYSTEM_LEASING_OPEN:
        console.log('Clicked x on leasing_open');
        break;
      default:
        break;
    }
  }

  public navigateToSource() {
    this.$emit('selected');
    switch (this.notification.type) {
      case MessageType.DEFAULT:
        this.$router.push({ name: 'Account:Messages:Thread', params: { id: this.notification.thread } });
        break;
      case MessageType.SYSTEM_LEASING_OPEN:
        this.$router.push({ name: 'Account:Dashboard' });
        break;
      case MessageType.SYSTEM_LEASING_CANCELLED:
        this.$router.push({ name: 'Account:Dashboard' });
        break;
      case MessageType.SYSTEM_LEASING_RESERVED:
        this.$router.push({ name: 'Account:Leasings' });
        break;
      case MessageType.SYSTEM_LEASING_REJECTED:
        this.$router.push({ name: 'Account:Leasings' });
        break;
      default:
        break;
    }
  }
}
