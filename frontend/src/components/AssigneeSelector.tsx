import { Fragment, useState } from 'react';
import { Combobox, Transition } from '@headlessui/react';
import { Check, ChevronsUpDown, User, X } from 'lucide-react';
import type { ProjectMember } from '../types/project';

interface AssigneeSelectorProps {
  members: ProjectMember[];
  selectedUserId?: number;
  onChange: (userId: number | undefined) => void;
  disabled?: boolean;
}

export function AssigneeSelector({ members, selectedUserId, onChange, disabled }: AssigneeSelectorProps) {
  const [query, setQuery] = useState('');

  const selectedMember = members.find((m) => m.userId === selectedUserId);

  const filteredMembers =
    query === ''
      ? members
      : members.filter((member) =>
          member.fullName.toLowerCase().includes(query.toLowerCase()) ||
          member.username.toLowerCase().includes(query.toLowerCase()) ||
          member.email.toLowerCase().includes(query.toLowerCase())
        );

  const getInitials = (fullName: string) => {
    return fullName
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-2">
        <User className="w-4 h-4 inline mr-1" />
        Assignee
      </label>
      <Combobox value={selectedUserId} onChange={onChange} disabled={disabled}>
        <div className="relative">
          <div className="relative w-full cursor-default overflow-hidden rounded-lg bg-white border border-gray-300 text-left focus-within:ring-2 focus-within:ring-primary-500">
            {selectedMember ? (
              <div className="flex items-center gap-2 px-3 py-2">
                <div className="w-6 h-6 bg-gradient-to-br from-primary-500 to-primary-600 rounded-full flex items-center justify-center">
                  <span className="text-xs text-white font-medium">
                    {getInitials(selectedMember.fullName)}
                  </span>
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">{selectedMember.fullName}</p>
                </div>
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    onChange(undefined);
                  }}
                  className="p-1 text-gray-400 hover:text-gray-600 rounded"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <>
                <Combobox.Input
                  className="w-full border-none py-2 pl-3 pr-10 text-sm leading-5 text-gray-900 focus:ring-0"
                  placeholder="Search members..."
                  displayValue={() => ''}
                  onChange={(event) => setQuery(event.target.value)}
                />
                <Combobox.Button className="absolute inset-y-0 right-0 flex items-center pr-2">
                  <ChevronsUpDown className="h-5 w-5 text-gray-400" aria-hidden="true" />
                </Combobox.Button>
              </>
            )}
          </div>
          <Transition
            as={Fragment}
            leave="transition ease-in duration-100"
            leaveFrom="opacity-100"
            leaveTo="opacity-0"
            afterLeave={() => setQuery('')}
          >
            <Combobox.Options className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm">
              {filteredMembers.length === 0 && query !== '' ? (
                <div className="relative cursor-default select-none py-2 px-4 text-gray-700">
                  No members found.
                </div>
              ) : (
                filteredMembers.map((member) => (
                  <Combobox.Option
                    key={member.userId}
                    className={({ active }) =>
                      `relative cursor-pointer select-none py-2 pl-10 pr-4 ${
                        active ? 'bg-primary-600 text-white' : 'text-gray-900'
                      }`
                    }
                    value={member.userId}
                  >
                    {({ selected, active }) => (
                      <>
                        <div className="flex items-center gap-2">
                          <div className={`w-6 h-6 bg-gradient-to-br from-primary-500 to-primary-600 rounded-full flex items-center justify-center`}>
                            <span className="text-xs text-white font-medium">
                              {getInitials(member.fullName)}
                            </span>
                          </div>
                          <div>
                            <span className={`block truncate ${selected ? 'font-medium' : 'font-normal'}`}>
                              {member.fullName}
                            </span>
                            <span className={`block text-xs ${active ? 'text-primary-100' : 'text-gray-500'}`}>
                              @{member.username}
                            </span>
                          </div>
                        </div>
                        {selected ? (
                          <span
                            className={`absolute inset-y-0 left-0 flex items-center pl-3 ${
                              active ? 'text-white' : 'text-primary-600'
                            }`}
                          >
                            <Check className="h-5 w-5" aria-hidden="true" />
                          </span>
                        ) : null}
                      </>
                    )}
                  </Combobox.Option>
                ))
              )}
            </Combobox.Options>
          </Transition>
        </div>
      </Combobox>
    </div>
  );
}

