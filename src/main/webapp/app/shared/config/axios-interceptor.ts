import axios from 'axios';

import { GARDLE_TOKEN_NAME, SERVER_API_URL } from '@/constants';
import { escapeData } from '@/shared/utils';

const TIMEOUT = 1000000;

const onRequestSuccess = config => {
  const token = localStorage.getItem(GARDLE_TOKEN_NAME) || sessionStorage.getItem(GARDLE_TOKEN_NAME);
  if (token) {
    if (!config.headers) {
      config.headers = {};
    }
    config.headers.Authorization = `Bearer ${token}`;
  }
  config.timeout = TIMEOUT;
  config.url = `${SERVER_API_URL}${config.url}`;

  if (config.method === 'post' || config.method === 'put') {
    config.data = escapeData(config.data);
  }
  return config;
};

const isRouteBeforeAuthenticated = uri => {
  return ['/api/v1/authenticate', '/api/v1/messages'].includes(uri);
};

const setupAxiosInterceptors = (onUnauthenticated, onError) => {
  const ignoreResponseError = responseError => {
    const ignoredError = ['COVER_NOT_FOUND'];
    return ignoredError.find(t => t === responseError.title);
  };

  const onResponseError = err => {
    const status = err.status || err.response.status;
    if (ignoreResponseError(err.response.data)) {
      return new Promise((resolve, reject) => reject(err));
    }
    if (status === 401) {
      if (isRouteBeforeAuthenticated(err.config.url)) {
        onError(err.response);
      } else {
        onUnauthenticated();
      }
    } else {
      onError(err.response);
    }
    return new Promise(() => {});
  };
  if (axios.interceptors) {
    axios.interceptors.request.use(onRequestSuccess);
    axios.interceptors.response.use(res => res, onResponseError);
  }
};

export { onRequestSuccess, setupAxiosInterceptors };
