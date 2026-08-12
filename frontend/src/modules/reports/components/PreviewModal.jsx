import React from "react";
import { FaTimes } from "react-icons/fa";

function PreviewModal({ isOpen, onClose, pdfBlobUrl, reportName }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm p-4 animate-fade-in">
      <div className="relative w-full max-w-4xl h-[90vh] bg-slate-900 rounded-[24px] shadow-[0_20px_50px_rgba(0,0,0,0.6)] flex flex-col overflow-hidden border border-slate-800">
        
        {/* Modal Header */}
        <div className="flex justify-between items-center px-8 py-5 border-b border-slate-800 bg-slate-950/20">
          <h3 className="text-lg font-bold text-white">
            PDF Preview - {reportName}
          </h3>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white transition-colors duration-200 p-1"
          >
            <FaTimes size={18} />
          </button>
        </div>

        {/* PDF Frame */}
        <div className="flex-1 bg-slate-950 p-4">
          {pdfBlobUrl ? (
            <iframe
              src={pdfBlobUrl}
              className="w-full h-full border-0 rounded-2xl bg-slate-900"
              title="PDF Preview"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <span className="text-slate-400 flex items-center gap-2">
                <svg className="animate-spin h-5 w-5 text-sky-400" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                Loading PDF document...
              </span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default PreviewModal;
