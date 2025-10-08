import { Fragment, useState } from 'react';
import { Listbox, Transition } from '@headlessui/react';
import { Check, Tag, X } from 'lucide-react';
import type { Label } from '../types/project';

interface LabelsSelectorProps {
  labels: Label[];
  selectedLabelIds: number[];
  onChange: (labelIds: number[]) => void;
  disabled?: boolean;
}

export function LabelsSelector({ labels, selectedLabelIds, onChange, disabled }: LabelsSelectorProps) {
  const [isOpen, setIsOpen] = useState(false);

  const selectedLabels = labels.filter((label) => selectedLabelIds.includes(label.id));

  const toggleLabel = (labelId: number) => {
    if (selectedLabelIds.includes(labelId)) {
      onChange(selectedLabelIds.filter((id) => id !== labelId));
    } else {
      onChange([...selectedLabelIds, labelId]);
    }
  };

  const removeLabel = (labelId: number) => {
    onChange(selectedLabelIds.filter((id) => id !== labelId));
  };

  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-2">
        <Tag className="w-4 h-4 inline mr-1" />
        Labels
      </label>
      <div className="space-y-2">
        {/* Selected Labels */}
        {selectedLabels.length > 0 && (
          <div className="flex flex-wrap gap-1">
            {selectedLabels.map((label) => (
              <span
                key={label.id}
                className="inline-flex items-center gap-1 px-2 py-1 text-xs rounded-full"
                style={{
                  backgroundColor: `${label.color}20`,
                  color: label.color,
                  border: `1px solid ${label.color}`,
                }}
              >
                {label.name}
                {!disabled && (
                  <button
                    type="button"
                    onClick={() => removeLabel(label.id)}
                    className="hover:opacity-70"
                  >
                    <X className="w-3 h-3" />
                  </button>
                )}
              </span>
            ))}
          </div>
        )}

        {/* Label Selector */}
        <Listbox value={selectedLabelIds} onChange={onChange} multiple disabled={disabled}>
          <div className="relative">
            <Listbox.Button
              onClick={() => setIsOpen(!isOpen)}
              className="relative w-full cursor-pointer rounded-lg bg-white border border-gray-300 py-2 pl-3 pr-10 text-left focus:outline-none focus:ring-2 focus:ring-primary-500 sm:text-sm"
            >
              <span className="block truncate text-gray-500">
                {selectedLabels.length === 0 ? 'Select labels...' : `${selectedLabels.length} label(s) selected`}
              </span>
              <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                <Tag className="h-5 w-5 text-gray-400" aria-hidden="true" />
              </span>
            </Listbox.Button>
            <Transition
              show={isOpen}
              as={Fragment}
              leave="transition ease-in duration-100"
              leaveFrom="opacity-100"
              leaveTo="opacity-0"
            >
              <Listbox.Options className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm">
                {labels.length === 0 ? (
                  <div className="relative cursor-default select-none py-2 px-4 text-gray-700">
                    No labels available.
                  </div>
                ) : (
                  labels.map((label) => (
                    <Listbox.Option
                      key={label.id}
                      className={({ active }) =>
                        `relative cursor-pointer select-none py-2 pl-10 pr-4 ${
                          active ? 'bg-primary-50' : ''
                        }`
                      }
                      value={label.id}
                      onClick={(e) => {
                        e.preventDefault();
                        toggleLabel(label.id);
                      }}
                    >
                      {({ selected }) => (
                        <>
                          <div className="flex items-center gap-2">
                            <span
                              className="px-2 py-1 text-xs rounded-full"
                              style={{
                                backgroundColor: `${label.color}20`,
                                color: label.color,
                                border: `1px solid ${label.color}`,
                              }}
                            >
                              {label.name}
                            </span>
                          </div>
                          {selected ? (
                            <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-primary-600">
                              <Check className="h-5 w-5" aria-hidden="true" />
                            </span>
                          ) : null}
                        </>
                      )}
                    </Listbox.Option>
                  ))
                )}
              </Listbox.Options>
            </Transition>
          </div>
        </Listbox>
      </div>
    </div>
  );
}

