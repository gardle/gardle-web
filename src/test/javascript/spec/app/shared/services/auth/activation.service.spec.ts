import axios from 'axios';
import ActivationService from '@/shared/services/auth/activation.service';

const mockedAxios: any = axios;

jest.mock('axios', () => ({
  get: jest.fn()
}));

describe('Authentication service', () => {
  let activationService: ActivationService;

  beforeEach(() => {
    mockedAxios.get.mockReset();

    activationService = new ActivationService();
  });

  it('should activate account', () => {
    mockedAxios.get.mockReturnValue(Promise.resolve());

    const activateCall = async () => {
      return await activationService.activate('validKey');
    };

    expect(activateCall()).resolves.toEqual({});
  });

  /* TODO it('should throw UserActivationError', () => {
    mockedAxios.get.mockReturnValue(
      Promise.reject({
        response: {
          status: 500
        }
      })
    );

    const activateCall = async () => {
      return await activationService.activate('invalidKey');
    };

    // expect(activateCall()).rejects;
  });*/

  it('should reject unknown errors', () => {
    mockedAxios.get.mockReturnValue(
      Promise.reject({
        response: {
          status: 422
        }
      })
    );

    const activateCall = async () => {
      return await activationService.activate('validKey');
    };

    expect(activateCall()).rejects.toBeTruthy();
  });
});
