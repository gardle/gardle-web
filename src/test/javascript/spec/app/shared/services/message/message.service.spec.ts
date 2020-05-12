import axios from 'axios';
import MessageService from '@/shared/services/Message/message.service';
import { PageModel } from '@/shared/model/page.model';
import { OffsetPageModel } from '@/shared/model/offsetPage.model';

const mockedAxios: any = axios;

const threadResponse = {
  content: [
    {
      content: 'Animi beatae doloremque consequatur excepturi dolorum est.',
      type: 'USER',
      userFrom: {
        id: 51,
        login: 'user',
        firstName: 'User',
        lastName: 'User',
        email: 'user@localhost'
      },
      userTo: {
        id: 65,
        login: 'nellie.kessler',
        firstName: 'Ella',
        lastName: 'Schmidt',
        email: 'harry.hettinger@hotmail.com'
      },
      thread: 'baf63361-ce38-4de2-9973-59d7cddaba9d',
      createdDate: '2019-12-02T13:40:08.879898Z'
    },
    {
      content: 'Dolorem expedita enim sint soluta et.',
      type: 'USER',
      userFrom: {
        id: 72,
        login: 'claud.hackett',
        firstName: 'Roxane',
        lastName: 'Schmidt',
        email: 'garett.hills@yahoo.com'
      },
      userTo: {
        id: 51,
        login: 'user',
        firstName: 'User',
        lastName: 'User',
        email: 'user@localhost'
      },
      thread: '8d545289-a1bb-4cda-b707-a6aa2dca0751',
      createdDate: '2019-12-02T13:40:10.929310Z'
    }
  ],
  pageable: {
    sort: {
      sorted: false,
      unsorted: true,
      empty: true
    },
    pageSize: 10,
    pageNumber: 0,
    offset: 0,
    paged: true,
    unpaged: false
  },
  totalPages: 1,
  totalElements: 2,
  last: false,
  first: true,
  sort: {
    sorted: false,
    unsorted: true,
    empty: true
  },
  number: 0,
  numberOfElements: 10,
  size: 10,
  empty: false
};

const notificationResponse = [
  {
    content: 'Dolorem expedita enim sint soluta et.',
    type: 'USER',
    userFrom: {
      id: 72,
      login: 'claud.hackett',
      firstName: 'Roxane',
      lastName: 'Schmidt',
      email: 'garett.hills@yahoo.com'
    },
    userTo: {
      id: 51,
      login: 'user',
      firstName: 'User',
      lastName: 'User',
      email: 'user@localhost'
    },
    thread: '8d545289-a1bb-4cda-b707-a6aa2dca0751',
    createdDate: '2019-12-02T13:40:10.929310Z'
  }
];

describe('Message service', () => {
  let messageService: MessageService;

  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.post.mockReset();
    mockedAxios.delete.mockReset();

    messageService = new MessageService();
  });

  it('getLatestMessageInPage: should return page', () => {
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        response: {
          data: threadResponse
        }
      })
    );

    const page: any = new PageModel(this.currentPage, this.pageSize);

    const getMessagesPageCall = async () => {
      messageService.getLatestMessageInThreadsPaged(page);
    };

    expect(getMessagesPageCall()).resolves.toEqual(threadResponse);
    expect(mockedAxios.get).toBeCalledWith(`/api/v1/messages?page=${page.pageNumber}&size=${page.pageSize}&sort=createdDate,desc`);
  });

  it('getMessagesByThreadPaged: should return page', () => {
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        response: {
          data: threadResponse
        }
      })
    );

    const threadId = 'baf63361-ce38-4de2-9973-59d7cddaba9d';

    const page: any = new OffsetPageModel(this.currentPage, this.pageSize, this.currentOffset);

    const getAllMessagesByThreadCall = async () => {
      messageService.getAllMessagesByThreadPaged(threadId, page);
    };

    expect(getAllMessagesByThreadCall()).resolves.toEqual(threadResponse);
    expect(mockedAxios.get).toBeCalledWith(
      `/api/v1/messages/thread/${threadId}?page=${page.pageNumber}&size=${page.pageSize}&offset=${page.pageOffset}&sort=createdDate,desc`
    );
  });

  it('getUnreadNotifications: should return list', () => {
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        response: {
          data: notificationResponse
        }
      })
    );

    const getUnreadNotificationsCall = async () => {
      messageService.getUnreadNotifications();
    };

    expect(getUnreadNotificationsCall()).resolves.toEqual(notificationResponse);
    expect(mockedAxios.get).toBeCalledWith(`/api/v1/messages/unread/`);
  });
});
