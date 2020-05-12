import axios from 'axios';
import { User } from '@/shared/model/user.model';

const accountApiClient = {
  postAccount(data: User) {
    return axios.post('/api/v1/account', data);
  },
  getAccount() {
    return axios.get('/api/v1/account');
  },
  changePassword(currentPassword: string, newPassword: string) {
    return axios.post('/api/v1/account/change-password', {
      currentPassword,
      newPassword
    });
  }
};

export default class AccountService {
  public save(account: User): Promise<any> {
    return new Promise((resolve, reject) => {
      accountApiClient
        .postAccount(account)
        .then(response => {
          resolve(response);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public getAccount(): Promise<User> {
    return new Promise<User>((resolve, reject) => {
      accountApiClient
        .getAccount()
        .then(response => {
          const user: User = response.data;
          resolve(user);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public changePassword(currentPassword: string, newPassword: string): Promise<any> {
    return new Promise((resolve, reject) => {
      accountApiClient
        .changePassword(currentPassword, newPassword)
        .then(response => {
          resolve(response);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }
}
