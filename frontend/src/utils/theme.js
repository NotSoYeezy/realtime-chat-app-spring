export function applyTheme() {
  const saved = localStorage.theme; // 'light', 'dark' lub undefined
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

  if (saved === 'dark') {
    document.documentElement.classList.add('dark');
  }
  else if (saved === 'light') {
    document.documentElement.classList.remove('dark');
  }
  else {
    // fallback: system
    if (prefersDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }
}

export function toggleTheme() {
  const saved = localStorage.theme;

  if (saved === 'dark') {
    localStorage.theme = 'light';
  } else if (saved === 'light') {
    localStorage.theme = 'dark';
  } else {
    // jeśli system → ciemny tymczasowo
    localStorage.theme = 'dark';
  }

  applyTheme();
}
