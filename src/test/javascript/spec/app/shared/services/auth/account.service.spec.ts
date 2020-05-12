import axios from 'axios';
import AccountService from '@/shared/services/auth/account.service';

const mockedAxios: any = axios;

jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn()
}));

const account1 = {
  id: 1234567,
  login: 'user',
  firstName: 'VName',
  lastName: 'NName',
  email: 'test@user.com',
  activated: true,
  langKey: 'en',
  authorities: [],
  createdBy: 'admin',
  createdDate: new Date(),
  lastModifiedBy: 'admin',
  lastModifiedDate: new Date(),
  password: 'passwdhash'
};

const currentPassword = 'oldpasswd';
const newPassword = 'newpasswd';

describe('Account service', () => {
  let accountService: AccountService;

  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.post.mockReset();

    accountService = new AccountService();
  });

  it('getAccount: should get account', () => {
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: account1
      })
    );

    const getAccountCall = async () => {
      return await accountService.getAccount();
    };

    expect(getAccountCall()).resolves.toEqual(account1);
  });

  it('save: posts account', async () => {
    mockedAxios.post.mockReturnValue(Promise.resolve({}));

    await accountService.save(account1);

    expect(mockedAxios.post).toBeCalledWith('/api/v1/account', account1);
  });

  it('changePassword: posts password change', async () => {
    mockedAxios.post.mockReturnValue(Promise.resolve({}));

    await accountService.changePassword(currentPassword, newPassword);

    expect(mockedAxios.post).toBeCalledWith('/api/v1/account/change-password', {
      currentPassword,
      newPassword
    });
  });

  /* TODO it('changePassword: should throw PasswordNotMatchingCriteriaError', () => {
    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: {
          status: 400,
          data: 'Incorrect password'
        }
      })
    );

    const changePasswordCall = async () => {
      return await accountService.changePassword(currentPassword, newPassword);
    };

    // expect(changePasswordCall()).rejects;
  });*/

  /* TODO it('save: should throw EmailAlreadyExistsError', () => {
    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: {
          status: 400,
          data: {
            errorKey: 'emailexists'
          }
        }
      })
    );

    const saveCall = async () => {
      return await accountService.save(account1);
    };

    // expect(saveCall()).rejects;
  }); */
});
