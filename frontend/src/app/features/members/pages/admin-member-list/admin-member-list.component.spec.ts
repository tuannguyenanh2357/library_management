import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminMemberList } from './admin-member-list';

describe('AdminMemberList', () => {
  let component: AdminMemberList;
  let fixture: ComponentFixture<AdminMemberList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminMemberList],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminMemberList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
