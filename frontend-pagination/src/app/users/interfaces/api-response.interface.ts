export interface ApiResponse<T> {
  timeStamp:  Date;
  statusCode: number;
  status:     string;
  message:    string;
  data:       T;
}

export interface Page {
  content:          User[];
  pageable:         Pageable;
  last:             boolean;
  totalPages:       number;
  totalElements:    number;
  size:             number;
  number:           number;
  sort:             Sort;
  first:            boolean;
  numberOfElements: number;
  empty:            boolean;
}

export interface User {
  name:     string;
  address:  string;
  status:   Status;
  phone:    string;
  email:    string;
  imageUrl: string;
}

export enum Status {
  active = 'ACTIVE',
  banned = 'BANNED',
  pending = 'PENDING'
}

export interface Pageable {
  pageNumber: number;
  pageSize:   number;
  sort:       Sort;
  offset:     number;
  paged:      boolean;
  unpaged:    boolean;
}

export interface Sort {
  empty:    boolean;
  sorted:   boolean;
  unsorted: boolean;
}
