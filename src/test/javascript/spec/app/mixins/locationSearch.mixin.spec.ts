import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import { LocationSearch } from '@/mixins/locationSearch';
import axios from 'axios';

const localVue = createLocalVue();

const mockedAxios = axios as jest.Mocked<typeof axios>;

const component = {
  functional: false,
  mixins: [LocationSearch],
  render() {}
};

const validAddresses = [
  {
    place_id: '15810183',
    licence: 'https://locationiq.com/attribution',
    osm_type: 'node',
    osm_id: '1517146176',
    boundingbox: ['48.2064353', '48.2065353', '16.3479189', '16.3480189'],
    lat: '48.2064853',
    lon: '16.3479689',
    display_name: 'SportLaden 1070, 57, Lerchenfelder Straße, Lerchenfeld, KG Neubau, Vienna, 1070, Austria',
    class: 'shop',
    type: 'bicycle',
    importance: 0.311,
    icon: 'https://locationiq.org/static/images/mapicons/shopping_bicycle.p.20.png',
    address: {
      hamelet: 'Testhamlet',
      postcode: 1050,
      state: 'Teststate'
    }
  },
  {
    place_id: '144549309',
    licence: 'https://locationiq.com/attribution',
    osm_type: 'way',
    osm_id: '253240671',
    boundingbox: ['48.3569378', '48.3571213', '15.7198417', '15.7201057'],
    lat: '48.35702955',
    lon: '15.7199737',
    display_name: '57, Lerchenfelder Straße, Gemeinde Nußdorf ob der Traisen, Lower Austria, 3133, Austria',
    class: 'building',
    type: 'yes',
    importance: 0.311,
    address: {
      road: 'Testroad',
      house_number: 13,
      postcode: 1050,
      state: 'Teststate'
    }
  }
];

describe('locationSearch', () => {
  let wrapper: Wrapper<any>;

  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.get.mockReturnValue(Promise.resolve({}));
    mockedAxios.post.mockReset();

    // @ts-ignore
    wrapper = mount(component, {
      localVue,
      sync: false
    });

    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.clearAllTimers();
  });

  it('should be a Vue instance', () => {
    expect(wrapper.isVueInstance()).toBeTruthy();
  });

  it('should call debounced function correctly', () => {
    wrapper.vm.forwardGeocode = jest.fn();
    wrapper.vm.debounceSearch('test search');

    expect(wrapper.vm.forwardGeocode).toBeCalledTimes(0);

    jest.advanceTimersByTime(500);

    expect(wrapper.vm.forwardGeocode).toBeCalledTimes(1);

    for (let i = 0; i < 5; ++i) {
      wrapper.vm.debounceSearch(`test ${i}`);
      jest.advanceTimersByTime(470);
    }

    // function should not get called another time
    expect(wrapper.vm.forwardGeocode).toBeCalledTimes(1);

    jest.advanceTimersByTime(30);

    expect(wrapper.vm.forwardGeocode).toBeCalledTimes(2);
  });

  it('should not call forward search with less than 4 chars', () => {
    wrapper.vm.forwardGeocode = jest.fn();
    wrapper.vm.debounceSearch('tes');

    expect(wrapper.vm.forwardGeocode).toBeCalledTimes(0);

    jest.advanceTimersByTime(500);

    expect(wrapper.vm.forwardGeocode).toBeCalledTimes(0);
  });

  it('should return locations forward geocoded', async () => {
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: validAddresses
      })
    );

    const forwardSearch = async () => {
      return await wrapper.vm.forwardGeocode('test query');
    };

    await expect(forwardSearch()).resolves.toEqual(validAddresses);

    expect(wrapper.vm.items).toEqual(validAddresses);
    expect(wrapper.vm.addresses).toEqual([validAddresses[1]]);
  });

  it('should return single location when reverse geocoded', async () => {
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: validAddresses[0]
      })
    );

    const reverseSearch = async () => {
      return wrapper.vm.reverseGeocode(13.54, 16.54);
    };

    await expect(reverseSearch()).resolves.toEqual(validAddresses[0]);
    expect(wrapper.vm.items).toEqual([validAddresses[0]]);
    expect(wrapper.vm.addresses).toEqual([]);
  });
});
