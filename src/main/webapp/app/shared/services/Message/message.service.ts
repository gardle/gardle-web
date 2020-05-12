import axios from 'axios';
import { Message } from '@/shared/model/message.model';
import { PageModel } from '@/shared/model/page.model';
import { MessagePage } from '@/shared/model/messagePage.model';
import { OffsetPageModel } from '@/shared/model/offsetPage.model';

const messageApiClient = {
  getLatestMessageInThreadsPaged(page: PageModel) {
    return axios.get<MessagePage>(`/api/v1/messages?page=${page.pageNumber}&size=${page.pageSize}&sort=createdDate,desc`);
  },
  getMessagesByThreadPaged(thread: string, page: OffsetPageModel) {
    return axios.get(
      `/api/v1/messages/thread/${thread}?page=${page.pageNumber}&size=${page.pageSize}&offset=${page.pageOffset}&sort=createdDate,desc`
    );
  },
  sendMessage(msg: Message) {
    return axios.post('/api/v1/messages', msg);
  },
  markThreadOpened(thread: string) {
    return axios.put(`/api/v1/messages/thread/${thread}`);
  },
  markSystemMessagesOpened() {
    return axios.put('/api/v1/messages/unread/system');
  },
  getUnreadNotifications() {
    return axios.get('/api/v1/messages/unread/');
  }
};

export default class MessageService {
  public getLatestMessageInThreadsPaged(page: PageModel): Promise<MessagePage> {
    return new Promise<MessagePage>((resolve, reject) => {
      messageApiClient
        .getLatestMessageInThreadsPaged(page)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public getAllMessagesByThreadPaged(thread: string, page: OffsetPageModel): Promise<MessagePage> {
    return new Promise<MessagePage>((resolve, reject) => {
      messageApiClient
        .getMessagesByThreadPaged(thread, page)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public sendMessage(msg: Message): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      messageApiClient
        .sendMessage(msg)
        .then(res => {
          resolve(res);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public markThreadOpened(thread: string) {
    return new Promise<any>((resolve, reject) => {
      messageApiClient
        .markThreadOpened(thread)
        .then(res => {
          resolve(res);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public markSystemMessagesOpened() {
    return new Promise<any>((resolve, reject) => {
      messageApiClient
        .markSystemMessagesOpened()
        .then(res => {
          resolve(res);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public getUnreadNotifications(): Promise<Message[]> {
    return new Promise<any>((resolve, reject) => {
      messageApiClient
        .getUnreadNotifications()
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }
}
