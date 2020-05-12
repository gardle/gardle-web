import { IPage } from '@/shared/model/page.model';

export interface IOffsetPage extends IPage {
  pageOffset: number;
}

export class OffsetPageModel implements IOffsetPage {
  constructor(public pageNumber: number, public pageSize: number, public pageOffset: number) {}
}
