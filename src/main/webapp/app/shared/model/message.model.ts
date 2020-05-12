import { SimpleUser } from '@/shared/model/simpleUser.model';
import { Leasing } from '@/shared/model/leasing.model';

export const enum MessageType {
  DEFAULT = 'USER',
  SYSTEM = 'SYSTEM',
  SYSTEM_LEASING_OPEN = 'SYSTEM_LEASING_OPEN',
  SYSTEM_LEASING_RESERVED = 'SYSTEM_LEASING_RESERVED',
  SYSTEM_LEASING_REJECTED = 'SYSTEM_LEASING_REJECTED',
  SYSTEM_LEASING_CANCELLED = 'SYSTEM_LEASING_CANCELLED'
}

export interface IMessage {
  id?: number;
  content?: string;
  type?: MessageType;
  userFrom?: SimpleUser;
  userTo?: SimpleUser;
  thread?: string;
  createdDate?: string;
  opened?: boolean;
  leasing?: Leasing;
}

export class Message implements IMessage {
  constructor(
    public id?: number,
    public content?: string,
    public type?: MessageType,
    public userFrom?: SimpleUser,
    public userTo?: SimpleUser,
    public thread?: string,
    public createdDate?: string,
    public opened?: boolean,
    public leasing?: Leasing
  ) {
    this.id = id;
    this.userFrom = userFrom;
    this.userTo = userTo;
    this.createdDate = createdDate;
    this.opened = opened;
    this.leasing = leasing;
  }
}
