import { Component, Inject } from 'vue-property-decorator';
import Vue from 'vue';
import MessageService from '@/shared/services/Message/message.service';
import { Message, MessageType } from '@/shared/model/message.model';
import { EventBus } from '@/shared/eventbus/eventbus';
import { OffsetPageModel } from '@/shared/model/offsetPage.model';

@Component
export default class PageMessageThread extends Vue {
  thread: Message[] = [];
  msgDraft = '';
  msgUpdated = false;
  msgSent = false;
  myId = this.$store.getters.userId;
  userTo = null;
  userFrom = null;
  msgSending = false;
  pageSize = 20;
  currentPage = 0;
  currentOffset = 0;
  totalPages: number = null;
  totalElements: number = null;
  currentlyLoading = false;
  lastScrollOffset = null;
  lastScrollElementHeight = null;
  lastScrollOffsetBeforeInsertion = null;
  lastScrollElementHeightBeforeInsertion = null;
  pollInterval = null;
  threadHeight = this.calcThreadHeight();

  // Needed for html access
  userType = MessageType.DEFAULT;
  leasingOpenType = MessageType.SYSTEM_LEASING_OPEN;
  leasingCancelledType = MessageType.SYSTEM_LEASING_CANCELLED;
  leasingReservedType = MessageType.SYSTEM_LEASING_RESERVED;
  leasingRejectedType = MessageType.SYSTEM_LEASING_REJECTED;

  get threadStyle() {
    return {
      flex: '1 1 auto',
      maxHeight: `${this.threadHeight}px`
    };
  }

  @Inject('messageService')
  private messageService: () => MessageService;

  private resetVariables() {
    this.thread = [];
    this.msgDraft = '';
    this.msgUpdated = false;
    this.msgSent = false;
    this.userTo = null;
    this.userFrom = null;
    this.msgSending = false;
    this.currentPage = 0;
    this.currentOffset = 0;
    this.totalPages = null;
    this.totalElements = null;
    this.currentlyLoading = false;
    this.lastScrollOffset = null;
    this.lastScrollElementHeight = null;
    this.lastScrollOffsetBeforeInsertion = null;
    this.lastScrollElementHeightBeforeInsertion = null;
  }

  private async getMessages() {
    this.messageService()
      .getAllMessagesByThreadPaged(this.$route.params.id, new OffsetPageModel(this.currentPage, this.pageSize, this.currentOffset))
      .then(res => {
        if (this.currentPage > 0) {
          this.lastScrollOffsetBeforeInsertion = document.getElementById('threadView').scrollTop;
          this.lastScrollElementHeightBeforeInsertion = document.getElementById('threadView').scrollHeight;
        }
        this.thread = res.content.reverse().concat(this.thread);
        if (this.hasLastUnread()) {
          this.markThreadOpened(this.$route.params.id);
        }
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.findUserTo();
        this.currentlyLoading = false;
        this.currentPage++;
      })
      .catch(err => {
        this.currentlyLoading = false;
      });

    return;
  }

  private updateMessages() {
    this.messageService()
      .getAllMessagesByThreadPaged(this.$route.params.id, new OffsetPageModel(0, this.pageSize, 0))
      .then(res => {
        const unreadMessages: Message[] = res.content.filter(msg => msg.opened === false && msg.userFrom.id !== this.myId);
        this.currentOffset += unreadMessages.length;
        if (unreadMessages.length > 0) {
          this.lastScrollOffsetBeforeInsertion = document.getElementById('threadView').scrollTop;
          this.lastScrollElementHeightBeforeInsertion = document.getElementById('threadView').scrollHeight;
          this.thread = this.thread.concat(unreadMessages.reverse());
          this.markThreadOpened(this.$route.params.id);
        }
      });
  }

  private findUserTo() {
    const lastMsg = this.thread[0];
    if (this.myId === lastMsg.userFrom.id) {
      this.userFrom = lastMsg.userFrom;
      this.userTo = lastMsg.userTo;
    } else {
      this.userFrom = lastMsg.userTo;
      this.userTo = lastMsg.userFrom;
    }
  }

  async created() {
    await this.getMessages();
    this.pollInterval = setInterval(
      function() {
        if (!this.currentlyLoading) {
          this.updateMessages();
        }
      }.bind(this),
      2000
    );
  }

  private markThreadOpened(thread: string) {
    this.messageService()
      .markThreadOpened(thread)
      .then(res => {
        EventBus.$emit('thread-opened', { thread: this.$route.params.id });
      });
  }

  async beforeRouteUpdate(to, from, next) {
    this.resetVariables();
    next();
    await this.getMessages();
  }

  private hasLastUnread() {
    const lastMsg = this.thread[this.thread.length - 1];
    return lastMsg.userTo.id === this.myId && !lastMsg.opened;
  }

  public beforeDestroy() {
    clearInterval(this.pollInterval);
  }

  public updateThreadViewStyle() {
    this.threadHeight = this.calcThreadHeight();
  }

  calcThreadHeight() {
    return window.innerHeight - 2 * this.$vuetify.application.top - 110;
  }

  onScrolling(e) {
    this.lastScrollOffset = e.target.scrollTop;
    this.lastScrollElementHeight = e.target.scrollHeight - e.target.clientHeight;

    if (this.lastScrollOffset < 300 && !this.currentlyLoading && this.currentPage < this.totalPages) {
      this.currentlyLoading = true;
      this.getMessages();
    }
  }

  updated() {
    if (this.msgUpdated) {
      // workaround for not being able to exclude elements in the updated() method
      this.msgUpdated = false;
    } else {
      this.scrollFix();
    }
  }

  scrollFix() {
    if (this.currentPage === 1 || this.msgSent) {
      this.scrollToBottom();
      this.msgSent = false;
    } else {
      this.scrollToLastPosition();
    }
  }

  msgUpdate() {
    this.msgUpdated = true;
  }

  scrollToBottom() {
    document.getElementById('threadView').scrollTop = document.getElementById('threadView').scrollHeight;
  }

  scrollToLastPosition() {
    document.getElementById('threadView').scrollTop =
      this.lastScrollOffsetBeforeInsertion +
      (document.getElementById('threadView').scrollHeight - this.lastScrollElementHeightBeforeInsertion);
  }

  public handleMsgSend() {
    if (this.msgDraft !== '') {
      this.msgSending = true;
      this.msgUpdated = true;
      const newMsg: Message = {
        content: this.msgDraft,
        userTo: this.userTo,
        userFrom: {
          id: this.myId
        },
        type: MessageType.DEFAULT,
        createdDate: new Date().toISOString()
      };

      this.messageService()
        .sendMessage(newMsg)
        .then(res => {
          this.thread.push(newMsg);
          this.msgDraft = '';
          this.msgSending = false;
          this.msgSent = true;
          this.currentOffset++;
        });
    }
  }
}
