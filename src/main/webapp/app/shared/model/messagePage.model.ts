import { IMessage } from '@/shared/model/message.model';
import { PageModel } from '@/shared/model/page.model';

export interface IMessagePage {
  content?: IMessage[];
  pageable?: PageModel;
  totalPages?: number;
  totalElements?: number;
}

export class MessagePage implements IMessagePage {
  constructor(public pageable?: PageModel, public totalPages?: number, public totalElements?: number, public content?: IMessage[]) {}
}
