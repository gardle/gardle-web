import { Component, Inject } from 'vue-property-decorator';
import Vue from 'vue';
import { Message, MessageType } from '@/shared/model/message.model';
import MessageService from '@/shared/services/Message/message.service';
import { EventBus } from '@/shared/eventbus/eventbus';
import { MessagePage } from '@/shared/model/messagePage.model';
import { PageModel } from '@/shared/model/page.model';

@Component
export default class PageMessages extends Vue {
  private _loading = false;
  private myId = this.$store.getters.userId;
  pageSize = 10;
  messagePage: MessagePage = new MessagePage(new PageModel(0, this.pageSize));
  test = true;

  userType = MessageType.DEFAULT;
  leasingOpenType = MessageType.SYSTEM_LEASING_OPEN;
  leasingCancelledType = MessageType.SYSTEM_LEASING_CANCELLED;
  leasingReservedType = MessageType.SYSTEM_LEASING_RESERVED;
  leasingRejectedType = MessageType.SYSTEM_LEASING_REJECTED;

  @Inject('messageService')
  private messageService: () => MessageService;

  created() {
    this.loadThreads();
    EventBus.$on('refresh-messages', this.loadThreads);
  }

  get loading(): boolean {
    return this._loading;
  }

  public handleUpdatePageNumber(pageNumber) {
    this.messagePage.pageable.pageNumber = pageNumber - 1;
    this.loadThreads();
  }

  otherPersonInThread(msg: Message): string {
    let firstName = '';
    let lastName = '';
    if (this.isMsgFromMe(msg)) {
      firstName = msg.userTo.firstName;
      lastName = msg.userTo.lastName;
    } else {
      firstName = msg.userFrom.firstName;
      lastName = msg.userFrom.lastName;
    }
    return firstName + ' ' + lastName;
  }

  isMsgFromMe(msg: Message): boolean {
    return msg.userFrom.id === this.myId;
  }

  isThreadUnopened(msg: Message): boolean {
    if (this.isMsgFromMe(msg)) {
      return false;
    } else {
      if (!msg.opened) {
        return true;
      }
    }
  }

  markThreadOpened(thread: string) {
    this.messageService().markThreadOpened(thread);
  }

  private async loadThreads() {
    try {
      this.messagePage = await this.messageService().getLatestMessageInThreadsPaged(
        new PageModel(this.messagePage.pageable.pageNumber, this.messagePage.pageable.pageSize)
      );
      if (this.messagePage.totalPages === 0) {
        console.log('No threads found!');
      }
    } catch (e) {
      console.error(e);
      EventBus.$emit('add-notification', {
        text: this.$t('error.UNKNOWN_ERROR'),
        color: 'error',
        close: true,
        timeout: 6000
      });
    }
  }
}
