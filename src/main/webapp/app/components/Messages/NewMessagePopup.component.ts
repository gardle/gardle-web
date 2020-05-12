import Vue from 'vue';
import { Component, Inject, Prop } from 'vue-property-decorator';
import { EventBus } from '@/shared/eventbus/eventbus';
import MessageService from '@/shared/services/Message/message.service';
import { Message, MessageType } from '@/shared/model/message.model';

@Component
export default class NewMessagePopupComponent extends Vue {
  private newMsgDialog = false;
  private msgSending = false;
  myId = this.$store.getters.userId;
  private msgDraft = '';
  @Prop({ type: Object, required: true }) readonly owner: any;
  @Inject('messageService')
  private messageService: () => MessageService;

  created() {
    EventBus.$on('newMsgPopup', () => this.dialogOpened());
  }

  private dialogOpened() {
    this.newMsgDialog = true;
    this.msgDraft = '';
  }

  public handleMsgSend() {
    if (this.msgDraft !== '') {
      this.msgSending = true;
      const newMsg: Message = {
        content: this.msgDraft,
        userTo: this.owner,
        userFrom: {
          id: this.myId
        },
        type: MessageType.DEFAULT
      };

      this.messageService()
        .sendMessage(newMsg)
        .then(res => {
          this.newMsgDialog = false;
          this.msgDraft = '';
          this.msgSending = false;
          EventBus.$emit('add-notification', {
            text: this.$t('message.popup.success'),
            color: 'success',
            close: true,
            timeout: 6000
          });
        })
        .catch(err => {
          this.msgSending = false;
          if (err.response.status === 401) {
            EventBus.$emit('add-notification', {
              text: this.$t('message.popup.error401'),
              color: 'warning',
              close: true,
              timeout: 2000
            });
            return;
          }
          EventBus.$emit('add-notification', {
            text: this.$t('message.popup.error') + ': ' + err,
            color: 'error',
            close: true,
            timeout: 6000
          });
        });
    }
  }
}
