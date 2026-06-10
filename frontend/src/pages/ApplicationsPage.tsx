import { FormEvent, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  BriefcaseBusiness,
  ChevronLeft,
  ChevronRight,
  ExternalLink,
  LogOut,
  Plus,
  RotateCcw,
  Search,
  X,
} from "lucide-react";
import { Link } from "react-router-dom";
import { createApplication, getApplications } from "../api/applicationsApi";
import { useAuth } from "../auth/AuthContext";
import { Button } from "../components/Button";
import { InputField } from "../components/InputField";
import { LoadingState } from "../components/LoadingState";
import { SelectField } from "../components/SelectField";
import { TextAreaField } from "../components/TextAreaField";
import { useToast } from "../components/ToastProvider";
import type {
  ApplicationFilters,
  ApplicationSortField,
  ApplicationStatus,
  CreateJobApplicationRequest,
  JobApplication,
  SortDirection,
} from "../types/api";
import { getApiErrorMessage } from "../utils/errors";
import { formatDate, formatSalary, formatStatus } from "../utils/formatters";

const statuses: ApplicationStatus[] = [
  "APPLIED",
  "ONLINE_ASSESSMENT",
  "INTERVIEW",
  "OFFER",
  "REJECTED",
  "ACCEPTED",
];

const sortFields: Array<{ value: ApplicationSortField; label: string }> = [
  { value: "createdAt", label: "Created" },
  { value: "updatedAt", label: "Updated" },
  { value: "applicationDate", label: "Application date" },
  { value: "companyName", label: "Company" },
  { value: "jobTitle", label: "Job title" },
  { value: "status", label: "Status" },
];

const defaultFilters: ApplicationFilters = {
  status: "",
  company: "",
  search: "",
  page: 0,
  size: 10,
  sortField: "createdAt",
  sortDirection: "desc",
};

type ApplicationFormState = {
  companyName: string;
  jobTitle: string;
  location: string;
  salaryMin: string;
  salaryMax: string;
  jobLink: string;
  status: ApplicationStatus;
  applicationDate: string;
  notes: string;
};

const initialFormState: ApplicationFormState = {
  companyName: "",
  jobTitle: "",
  location: "",
  salaryMin: "",
  salaryMax: "",
  jobLink: "",
  status: "APPLIED",
  applicationDate: "",
  notes: "",
};

export function ApplicationsPage() {
  const { user, logout } = useAuth();
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<ApplicationFilters>(defaultFilters);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [form, setForm] = useState<ApplicationFormState>(initialFormState);
  const [formError, setFormError] = useState("");

  const applicationsQuery = useQuery({
    queryKey: ["applications", filters],
    queryFn: () => getApplications(filters),
  });

  const createMutation = useMutation({
    mutationFn: createApplication,
    onSuccess: () => {
      setForm(initialFormState);
      setFormError("");
      setIsCreateOpen(false);
      showToast("Application added");
      void queryClient.invalidateQueries({ queryKey: ["applications"] });
    },
    onError: (error) => {
      const message = getApiErrorMessage(error);
      setFormError(message);
      showToast(message, "error");
    },
  });

  const page = applicationsQuery.data;
  const applications = page?.content ?? [];
  const startItem = page && page.totalElements > 0 ? page.page * page.size + 1 : 0;
  const endItem = page ? Math.min((page.page + 1) * page.size, page.totalElements) : 0;

  const activeFilterCount = useMemo(
    () => [filters.status, filters.company, filters.search].filter(Boolean).length,
    [filters.company, filters.search, filters.status],
  );

  function updateFilter<Key extends keyof ApplicationFilters>(
    key: Key,
    value: ApplicationFilters[Key],
  ) {
    setFilters((current) => ({
      ...current,
      [key]: value,
      page: key === "page" ? Number(value) : 0,
    }));
  }

  function resetFilters() {
    setFilters(defaultFilters);
  }

  function submitCreateApplication(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError("");
    createMutation.mutate(toCreateRequest(form));
  }

  return (
    <main className="app-page">
      <header className="app-header">
        <div>
          <p className="eyebrow">JobTrackr</p>
          <h1>Applications</h1>
        </div>
        <div className="user-actions">
          <span>{user?.fullName}</span>
          <Button type="button" variant="ghost" onClick={logout}>
            <LogOut size={18} />
            Logout
          </Button>
        </div>
      </header>

      <section className="dashboard-shell">
        <div className="dashboard-toolbar">
          <div className="toolbar-search">
            <InputField
              id="search"
              label="Search"
              type="search"
              value={filters.search}
              onChange={(event) => updateFilter("search", event.target.value)}
              placeholder="Company, title, location, notes"
            />
            <Search className="toolbar-search-icon" size={18} aria-hidden="true" />
          </div>
          <SelectField
            id="status"
            label="Status"
            value={filters.status}
            onChange={(event) =>
              updateFilter("status", event.target.value as ApplicationStatus | "")
            }
          >
            <option value="">All statuses</option>
            {statuses.map((status) => (
              <option key={status} value={status}>
                {formatStatus(status)}
              </option>
            ))}
          </SelectField>
          <InputField
            id="company"
            label="Company"
            value={filters.company}
            onChange={(event) => updateFilter("company", event.target.value)}
            placeholder="Filter company"
          />
          <SelectField
            id="sortField"
            label="Sort by"
            value={filters.sortField}
            onChange={(event) =>
              updateFilter("sortField", event.target.value as ApplicationSortField)
            }
          >
            {sortFields.map((field) => (
              <option key={field.value} value={field.value}>
                {field.label}
              </option>
            ))}
          </SelectField>
          <SelectField
            id="sortDirection"
            label="Direction"
            value={filters.sortDirection}
            onChange={(event) =>
              updateFilter("sortDirection", event.target.value as SortDirection)
            }
          >
            <option value="desc">Newest first</option>
            <option value="asc">Oldest first</option>
          </SelectField>
          <div className="toolbar-actions">
            <Button type="button" variant="ghost" onClick={resetFilters}>
              <RotateCcw size={18} />
              Reset{activeFilterCount > 0 ? ` (${activeFilterCount})` : ""}
            </Button>
            <Button type="button" onClick={() => setIsCreateOpen(true)}>
              <Plus size={18} />
              Add
            </Button>
          </div>
        </div>

        {applicationsQuery.isError && (
          <div className="form-error">{getApiErrorMessage(applicationsQuery.error)}</div>
        )}

        <div className="applications-table-wrap">
          <table className="applications-table">
            <thead>
              <tr>
                <th>Role</th>
                <th>Status</th>
                <th>Location</th>
                <th>Salary</th>
                <th>Applied</th>
                <th>Link</th>
              </tr>
            </thead>
            <tbody>
              {applicationsQuery.isLoading && (
                <tr>
                  <td colSpan={6} className="table-state">
                    <LoadingState label="Loading applications..." />
                  </td>
                </tr>
              )}
              {!applicationsQuery.isLoading &&
                applications.map((application) => (
                  <ApplicationRow key={application.id} application={application} />
                ))}
              {!applicationsQuery.isLoading && applications.length === 0 && (
                <tr>
                  <td colSpan={6} className="table-state">
                    No applications found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <footer className="pagination-bar">
          <span>
            {startItem}-{endItem} of {page?.totalElements ?? 0}
          </span>
          <div className="pagination-actions">
            <Button
              type="button"
              variant="ghost"
              disabled={!page || page.first}
              onClick={() => updateFilter("page", Math.max(filters.page - 1, 0))}
              aria-label="Previous page"
            >
              <ChevronLeft size={18} />
            </Button>
            <span>
              Page {(page?.page ?? 0) + 1} of {Math.max(page?.totalPages ?? 1, 1)}
            </span>
            <Button
              type="button"
              variant="ghost"
              disabled={!page || page.last}
              onClick={() => updateFilter("page", filters.page + 1)}
              aria-label="Next page"
            >
              <ChevronRight size={18} />
            </Button>
          </div>
        </footer>
      </section>

      {isCreateOpen && (
        <div className="modal-backdrop" role="presentation">
          <section className="modal-panel" aria-labelledby="create-application-title">
            <header className="modal-header">
              <div>
                <p className="eyebrow">New application</p>
                <h2 id="create-application-title">Add application</h2>
              </div>
              <button
                type="button"
                className="icon-button"
                onClick={() => setIsCreateOpen(false)}
                aria-label="Close"
              >
                <X size={20} />
              </button>
            </header>

            <form className="application-form" onSubmit={submitCreateApplication}>
              {formError && <div className="form-error">{formError}</div>}
              <div className="form-grid">
                <InputField
                  id="companyName"
                  label="Company"
                  value={form.companyName}
                  onChange={(event) => setFormField("companyName", event.target.value)}
                  required
                />
                <InputField
                  id="jobTitle"
                  label="Job title"
                  value={form.jobTitle}
                  onChange={(event) => setFormField("jobTitle", event.target.value)}
                  required
                />
                <InputField
                  id="location"
                  label="Location"
                  value={form.location}
                  onChange={(event) => setFormField("location", event.target.value)}
                />
                <SelectField
                  id="createStatus"
                  label="Status"
                  value={form.status}
                  onChange={(event) =>
                    setFormField("status", event.target.value as ApplicationStatus)
                  }
                >
                  {statuses.map((status) => (
                    <option key={status} value={status}>
                      {formatStatus(status)}
                    </option>
                  ))}
                </SelectField>
                <InputField
                  id="salaryMin"
                  label="Salary min"
                  type="number"
                  min="0"
                  value={form.salaryMin}
                  onChange={(event) => setFormField("salaryMin", event.target.value)}
                />
                <InputField
                  id="salaryMax"
                  label="Salary max"
                  type="number"
                  min="0"
                  value={form.salaryMax}
                  onChange={(event) => setFormField("salaryMax", event.target.value)}
                />
                <InputField
                  id="applicationDate"
                  label="Application date"
                  type="date"
                  value={form.applicationDate}
                  onChange={(event) => setFormField("applicationDate", event.target.value)}
                />
                <InputField
                  id="jobLink"
                  label="Job link"
                  type="url"
                  value={form.jobLink}
                  onChange={(event) => setFormField("jobLink", event.target.value)}
                />
              </div>
              <TextAreaField
                id="notes"
                label="Notes"
                rows={4}
                value={form.notes}
                onChange={(event) => setFormField("notes", event.target.value)}
              />
              <div className="modal-actions">
                <Button type="button" variant="ghost" onClick={() => setIsCreateOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={createMutation.isPending}>
                  <Plus size={18} />
                  {createMutation.isPending ? "Adding..." : "Add application"}
                </Button>
              </div>
            </form>
          </section>
        </div>
      )}
    </main>
  );

  function setFormField<Key extends keyof ApplicationFormState>(
    key: Key,
    value: ApplicationFormState[Key],
  ) {
    setForm((current) => ({ ...current, [key]: value }));
  }
}

function ApplicationRow({ application }: { application: JobApplication }) {
  return (
    <tr className="clickable-row">
      <td>
        <div className="role-cell">
          <div className="role-icon" aria-hidden="true">
            <BriefcaseBusiness size={18} />
          </div>
          <div>
            <Link className="row-title-link" to={`/applications/${application.id}`}>
              {application.jobTitle}
            </Link>
            <span>{application.companyName}</span>
          </div>
        </div>
      </td>
      <td>
        <span className={`status-badge status-${application.status.toLowerCase()}`}>
          {formatStatus(application.status)}
        </span>
      </td>
      <td>{application.location || "Remote/unspecified"}</td>
      <td>{formatSalary(application.salaryMin, application.salaryMax)}</td>
      <td>{formatDate(application.applicationDate)}</td>
      <td>
        {application.jobLink ? (
          <a
            className="external-link"
            href={application.jobLink}
            target="_blank"
            rel="noreferrer"
            aria-label={`Open ${application.companyName} job link`}
          >
            <ExternalLink size={18} />
          </a>
        ) : (
          <span className="muted">None</span>
        )}
      </td>
    </tr>
  );
}

function toCreateRequest(form: ApplicationFormState): CreateJobApplicationRequest {
  return {
    companyName: form.companyName.trim(),
    jobTitle: form.jobTitle.trim(),
    location: optionalString(form.location),
    salaryMin: optionalNumber(form.salaryMin),
    salaryMax: optionalNumber(form.salaryMax),
    jobLink: optionalString(form.jobLink),
    status: form.status,
    applicationDate: optionalString(form.applicationDate),
    notes: optionalString(form.notes),
  };
}

function optionalString(value: string): string | null {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}

function optionalNumber(value: string): number | null {
  return value ? Number(value) : null;
}
