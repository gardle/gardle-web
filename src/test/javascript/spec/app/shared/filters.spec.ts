import * as config from '@/shared/config/config';
import { createLocalVue } from '@vue/test-utils';
import { addDays } from 'date-fns';

const localVue = createLocalVue();

config.initVueApp(localVue);

describe('filters', () => {
  it('should format numbers', () => {
    const numberPre = 24.4666666;
    const numberPost = '24.47';

    const numberFormatted = new localVue().$options.filters.formatNumber(numberPre);

    expect(numberFormatted).toEqual(numberPost);
  });

  it('should calc period price', () => {
    const from = new Date().toISOString();
    const to = addDays(new Date(), 5).toISOString();
    const fieldPrice = 10;

    const price = new localVue().$options.filters.calcPeriodPrice(fieldPrice, from, to);

    expect(price).toEqual(50);
  });

  it('should format date distance', () => {
    const to = addDays(new Date(), 5).toISOString();

    const dist = new localVue().$options.filters.humanizeDateDistance(to);

    expect(dist).toEqual('5 Tage');
  });

  it('should format date interval', () => {
    const from = new Date().toISOString();
    const to = addDays(new Date(), 5).toISOString();

    const dist = new localVue().$options.filters.humanizeDateInterval(from, to);

    expect(dist).toEqual('5 Tage');
  });
});
