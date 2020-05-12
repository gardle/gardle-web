import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import VueRouter from 'vue-router';
import Vuex from 'vuex';
import Vue from 'vue';

import store from '@/store/';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
import router from '@/router/index';
import DashboardComponent from '@/pages/Account/Dashboard/Dashboard.component';
import Dashboard from '@/pages/Account/Dashboard/Dashboard.vue';
import LeasingService from '@/shared/services/leasing/leasing.service';

const localVue = createLocalVue();
localVue.use(VueRouter);
localVue.use(Vuex);

const mockedAxios = axios as jest.Mocked<typeof axios>;

config.initVueApp(localVue);
const vuetify = vuetifyConfig.initVuetify(localVue);

const defaultMount = (compProps: object = null) => {
  return shallowMount<DashboardComponent>(Dashboard, {
    store,
    i18n,
    localVue,
    router,
    vuetify,
    sync: false,
    propsData: compProps,
    provide: {
      leasingService: () => new LeasingService()
    }
  });
};

const mockedLeasings = [
  {
    id: 1,
    from: '2020-01-25T23:00:00Z',
    to: '2020-02-16T22:59:00Z',
    status: 'OPEN',
    user: {
      id: 55,
      login: 'testuser',
      firstName: 'Test',
      lastName: 'User',
      email: 'testmail@mail.com'
    },
    messages: [],
    gardenField: {
      id: 1,
      name: 'Eins Dankfield'
    }
  },
  {
    id: 2,
    from: '2020-01-05T23:00:00Z',
    to: '2020-01-20T22:59:00Z',
    status: 'RESERVED',
    user: {
      id: 55,
      login: 'testuser',
      firstName: 'Test',
      lastName: 'User',
      email: 'testmail@mail.com'
    },
    messages: [],
    gardenField: {
      id: 5,
      name: 'Testfield'
    }
  }
];

const clearMocks = () => {
  mockedAxios.get.mockReset();
  mockedAxios.post.mockReset();
  mockedAxios.put.mockReset();
};

const mockCreatedCalls = () => {
  // mock listing requests
  mockedAxios.get
    .mockReturnValue(
      Promise.resolve({
        data: {
          content: [mockedLeasings[1]]
        }
      })
    )
    .mockReturnValueOnce(
      Promise.resolve({
        data: {
          content: [mockedLeasings[0]]
        }
      })
    );
};

describe('Dashboard component', () => {
  let wrapper: Wrapper<DashboardComponent>;

  beforeEach(async () => {
    clearMocks();
    mockCreatedCalls();

    wrapper = defaultMount();

    await Vue.nextTick();
    await Vue.nextTick();
    await Vue.nextTick();
  });

  it('should be created correctly', async () => {
    expect(wrapper.vm.requests).toEqual({
      content: [mockedLeasings[0]]
    });
    expect(wrapper.vm.currentLeasings).toEqual({
      content: [mockedLeasings[1]]
    });
  });

  it('should accept leasings correctly', async () => {
    mockedAxios.put.mockReturnValueOnce(
      Promise.resolve({
        data: {}
      })
    );

    mockedAxios.get.mockReturnValueOnce(
      Promise.resolve({
        data: {
          content: []
        }
      })
    );

    expect(wrapper.vm.requests).toEqual({
      content: [mockedLeasings[0]]
    });

    await wrapper.vm.acceptLeasingRequest(1, 1);

    await Vue.nextTick();

    expect(wrapper.vm.requests).toEqual({
      content: []
    });
  });

  it('should reject leasings correctly', async () => {
    mockedAxios.put.mockReturnValueOnce(
      Promise.resolve({
        data: {}
      })
    );

    mockedAxios.get.mockReturnValueOnce(
      Promise.resolve({
        data: {
          content: []
        }
      })
    );

    expect(wrapper.vm.requests).toEqual({
      content: [mockedLeasings[0]]
    });

    await wrapper.vm.rejectLeasingRequest(1, 1);

    await Vue.nextTick();

    expect(wrapper.vm.requests).toEqual({
      content: []
    });
  });
});
