import { GardleAPIException, mapException } from '@/shared/exceptions';
import { AxiosResponse } from 'axios';

describe('gardle exceptions', () => {
  it('should map exceptions correctly', () => {
    let axiosError = {
      data: {
        title: 'NOT_AN_IMAGE'
      },
      status: 400
    };

    let exc = mapException(<AxiosResponse>axiosError);

    expect(exc).toEqual(new GardleAPIException(axiosError.data.title, 'warning', 'mdi-alert'));

    // 401

    axiosError = {
      data: {
        title: 'USER_NOT_LOGGED_IN'
      },
      status: 401
    };

    exc = mapException(<AxiosResponse>axiosError);

    expect(exc).toEqual(new GardleAPIException(axiosError.data.title, 'warning', 'mdi-logout'));

    // 403

    axiosError = {
      data: {
        title: 'ACCESS_DENIED'
      },
      status: 403
    };

    exc = mapException(<AxiosResponse>axiosError);

    expect(exc).toEqual(new GardleAPIException(axiosError.data.title, 'error', 'mdi-cancel'));

    // 404

    axiosError = {
      data: {
        title: 'USER_NOT_FOUND'
      },
      status: 404
    };

    exc = mapException(<AxiosResponse>axiosError);

    expect(exc).toEqual(new GardleAPIException(axiosError.data.title, 'warning', 'mdi-cloud-question'));

    // 409

    axiosError = {
      data: {
        title: 'EMAIL_ALREADY_EXISTS'
      },
      status: 409
    };

    exc = mapException(<AxiosResponse>axiosError);

    expect(exc).toEqual(new GardleAPIException(axiosError.data.title, 'error', 'mdi-flash'));

    // 500

    axiosError = {
      data: {
        title: 'INTERNAL_SERVER_ERROR'
      },
      status: 500
    };

    exc = mapException(<AxiosResponse>axiosError);

    expect(exc).toEqual(new GardleAPIException(axiosError.data.title, 'error', 'mdi-server'));
  });
});
