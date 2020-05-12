export interface IPage {
  pageNumber: number;
  pageSize: number;
}

export class PageModel implements IPage {
  constructor(public pageNumber: number, public pageSize: number) {}
}
