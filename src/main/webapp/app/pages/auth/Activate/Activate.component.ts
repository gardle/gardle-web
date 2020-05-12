import { Component, Inject } from 'vue-property-decorator';
import Vue from 'vue';
import ActivationService from '@/shared/services/auth/activation.service';
import { EventBus } from '@/shared/eventbus/eventbus';

@Component
export default class PageActivate extends Vue {
  activated: boolean = null;

  loading = false;

  @Inject('activationService')
  private activationService: () => ActivationService;

  mounted() {
    if (this.$route.query.key) {
      this.loading = true;
      this.init(<string>this.$route.query.key);
    }
  }

  get hasActivationKey() {
    return this.$route.query['key'] !== undefined;
  }

  public async init(key: string) {
    try {
      const res = await this.activationService().activate(key);
      this.activated = true;
    } catch (e) {
      this.activated = false;

      if (e instanceof Error) {
        let err_data = null;
        switch (e.name) {
          default:
            err_data = {
              title_key: 'UNKNOWN_ERROR'
            };
        }
        EventBus.$emit('add-notification', {
          text: this.$t('error.' + err_data.title_key),
          color: 'error',
          close: true,
          timeout: 6000
        });
      }
    } finally {
      this.loading = false;
    }
  }

  public handleLoginClick() {
    EventBus.$emit('show-login');
  }
}
