import Vue from 'vue';
import Component from 'vue-class-component';
import { Prop } from 'vue-property-decorator';

@Component
export default class LeasingStatusSwitcher extends Vue {
  @Prop({ type: String })
  value;

  transitions = [
    {
      id: 1,
      type: 'OPEN',
      color: 'info',
      transitions: [2, 3]
    },
    {
      id: 2,
      type: 'RESERVED',
      color: 'success',
      transitions: []
    },
    {
      id: 3,
      type: 'REJECTED',
      color: 'error',
      transitions: []
    },
    {
      id: 4,
      type: 'CANCELLED',
      color: 'warning',
      transitions: []
    }
  ];

  get availableTransitions() {
    return this.currentStatus.transitions;
  }

  get currentStatus() {
    return this.transitions.filter(item => item.type === this.value)[0];
  }

  public updateLeasingStatus(status) {
    this.$emit('update-status', status);
  }
}
