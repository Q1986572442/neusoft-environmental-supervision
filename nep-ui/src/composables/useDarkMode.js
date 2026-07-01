import { ref, watchEffect } from 'vue'

const isDark = ref(localStorage.getItem('darkMode') === 'true')

export function useDarkMode() {
  function toggle() {
    isDark.value = !isDark.value
  }

  watchEffect(() => {
    const root = document.documentElement
    if (isDark.value) {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }
    localStorage.setItem('darkMode', isDark.value)
  })

  return { isDark, toggle }
}
